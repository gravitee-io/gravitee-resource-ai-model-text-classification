/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.resource.ai_model.downloader;

import io.gravitee.resource.ai_model.exception.ModelFileNotFoundException;
import io.gravitee.resource.ai_model.fetcher.VertxHuggingFaceClientRx;
import io.gravitee.resource.ai_model.model.ModelFile;
import io.gravitee.resource.ai_model.model.ModelFileType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.file.OpenOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HuggingFaceDownloaderService {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int IDLE_TIMEOUT = 10000;

    private static final String HOST = "https://huggingface.co";

    private final Vertx vertx;
    private final String modelName;

    private final VertxHuggingFaceClientRx modelDownloader;

    public HuggingFaceDownloaderService(Vertx vertx, String modelName) {
        var clientOptions = new WebClientOptions()
            .setName("gio-ibm-web-client")
            .setDefaultHost(HOST)
            .setDefaultPort(443)
            .setSsl(true)
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setIdleTimeout(IDLE_TIMEOUT)
            .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
            .setLogActivity(true);

        this.modelName = modelName;
        this.vertx = vertx;
        WebClient client = WebClient.create(vertx, clientOptions);
        this.modelDownloader = new VertxHuggingFaceClientRx(client);
    }

    public Maybe<Map<ModelFileType, String>> downloadModel(List<ModelFile> modelFiles) {
        return modelDownloader
            .listModelFiles(modelName)
            .toList()
            .flatMapPublisher(availableFiles ->
                Flowable
                    .fromIterable(modelFiles)
                    .flatMapMaybe(modelFile -> {
                        if (!availableFiles.contains(modelFile.name())) {
                            return Maybe.error(
                                new ModelFileNotFoundException(
                                    String.format("Model file not found on HuggingFace: %s for model: %s", modelFile.name(), modelName)
                                )
                            );
                        }

                        return vertx
                            .fileSystem()
                            .rxExists(modelFile.name())
                            .flatMapMaybe(exists -> {
                                if (exists) {
                                    log.info("Skipping download; file already exists: {}", modelFile.name());
                                    return Maybe.just(Map.entry(modelFile.type(), modelFile.name()));
                                }

                                return vertx
                                    .fileSystem()
                                    .rxOpen(modelFile.name(), new OpenOptions().setCreate(true).setWrite(true).setTruncateExisting(true))
                                    .flatMapMaybe(file ->
                                        modelDownloader
                                            .downloadModelFile(modelName, modelFile.name(), file)
                                            .doFinally(file::close)
                                            .andThen(Maybe.just(Map.entry(modelFile.type(), modelFile.name())))
                                    )
                                    .doOnSuccess(resp -> log.info("Download completed successfully: {}", modelFile.name()))
                                    .doOnError(err -> log.error("Download failed: {}", err.getMessage()));
                            });
                    })
            )
            .collect(
                () -> new EnumMap<ModelFileType, String>(ModelFileType.class),
                (map, entry) -> map.put(entry.getKey(), entry.getValue())
            )
            .map(map -> (Map<ModelFileType, String>) map)
            .filter(map -> !map.isEmpty());
    }
}
