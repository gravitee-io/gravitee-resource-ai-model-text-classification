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

import io.gravitee.resource.ai_model.exception.ModelDownloadFailedException;
import io.gravitee.resource.ai_model.exception.ModelFileNotFoundException;
import io.gravitee.resource.ai_model.fetcher.VertxHuggingFaceClientRx;
import io.gravitee.resource.ai_model.model.ModelFile;
import io.gravitee.resource.ai_model.model.ModelFileType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.file.OpenOptions;
import io.vertx.rxjava3.core.Vertx;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HuggingFaceDownloaderService {

    private final Vertx vertx;

    private final VertxHuggingFaceClientRx modelDownloader;

    public HuggingFaceDownloaderService(Vertx vertx, VertxHuggingFaceClientRx client) {
        this.vertx = vertx;
        this.modelDownloader = client;
    }

    public Single<Map<ModelFileType, String>> downloadModel(String modelName, List<ModelFile> modelFiles, Path modelDirectory) {
        return modelDownloader
            .listModelFiles(modelName)
            .toList()
            .flatMapPublisher(availableFiles ->
                Flowable
                    .fromIterable(modelFiles)
                    .flatMapSingle(modelFile -> {
                        if (!availableFiles.contains(modelFile.name())) {
                            return Single.error(
                                new ModelFileNotFoundException(
                                    String.format("Model file not found on HuggingFace: %s for model: %s", modelFile.name(), modelName)
                                )
                            );
                        }

                        Path outputPath = modelDirectory.resolve(modelFile.name());

                        return vertx
                            .fileSystem()
                            .rxExists(outputPath.toString())
                            .flatMap(exists -> {
                                if (exists) {
                                    log.info("Skipping download; file already exists: {}", modelFile.name());
                                    return Single.just(Map.entry(modelFile.type(), outputPath.toString()));
                                }

                                return vertx
                                    .fileSystem()
                                    .rxOpen(
                                        outputPath.toString(),
                                        new OpenOptions().setCreate(true).setWrite(true).setTruncateExisting(true)
                                    )
                                    .flatMap(file ->
                                        modelDownloader
                                            .downloadModelFile(modelName, modelFile.name(), file)
                                            .doFinally(file::close)
                                            .andThen(Single.just(Map.entry(modelFile.type(), outputPath.toString())))
                                            .onErrorResumeNext(err ->
                                                Single.error(
                                                    new ModelDownloadFailedException(
                                                        String.format("Download failed for model: %s", modelFile.name()),
                                                        err
                                                    )
                                                )
                                            )
                                    )
                                    .doOnSuccess(resp -> log.info("Download completed successfully: {}", modelFile.name()))
                                    .doOnError(err -> log.error("Download failed", err));
                            });
                    })
            )
            .collect(() -> new EnumMap<>(ModelFileType.class), (map, entry) -> map.put(entry.getKey(), entry.getValue()));
    }
}
