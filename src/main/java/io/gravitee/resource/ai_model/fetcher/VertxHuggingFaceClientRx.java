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
package io.gravitee.resource.ai_model.fetcher;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.streams.WriteStream;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.rxjava3.ext.web.codec.BodyCodec;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxHuggingFaceClientRx implements HuggingFaceClientRx {

    private static final String SIBLINGS_KEY = "siblings";
    private static final String RFILENAME_KEY = "rfilename";

    private static final String HOST = "https://huggingface.co";
    private static final String HF_API_URL = HOST + "/api/models/%s";
    private static final String HF_REPO_BASE_URL = HOST + "/%s/resolve/main/%s";

    private final WebClient client;

    public VertxHuggingFaceClientRx(WebClient client) {
        this.client = client;
    }

    @Override
    public Flowable<String> listModelFiles(String modelName) {
        var request = client.requestAbs(HttpMethod.GET, String.format(HF_API_URL, modelName));
        return request
            .rxSend()
            .map(response -> response.body().toJsonObject().getJsonArray(SIBLINGS_KEY))
            .flattenAsFlowable(VertxHuggingFaceClientRx::getFileNames)
            .doOnError(t -> log.error("An unexpected error has occurred while list model repository [{}]: {}", modelName, t.getMessage(), t)
            )
            .onErrorComplete();
    }

    @Override
    public Completable downloadModelFile(String modelName, String fileName, WriteStream<Buffer> file) {
        log.info("Downloading file [{}] from model [{}]", fileName, modelName);
        var request = client.requestAbs(HttpMethod.GET, String.format(HF_REPO_BASE_URL, modelName, fileName));

        return request
            .addQueryParam("download", "true")
            .followRedirects(true)
            .as(BodyCodec.pipe(file))
            .rxSend()
            .ignoreElement()
            .doOnComplete(() -> log.info("Downloaded model file [{}] successfully", fileName))
            .doOnError(err -> log.error("Failed to download [{}]: {}", fileName, err.getMessage(), err));
    }

    private static List<String> getFileNames(JsonArray siblings) {
        return siblings.stream().map(obj -> ((JsonObject) obj).getString(RFILENAME_KEY)).toList();
    }
}
