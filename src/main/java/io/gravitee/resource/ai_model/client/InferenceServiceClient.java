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
package io.gravitee.resource.ai_model.client;

import static io.gravitee.inference.api.Constants.CLASSIFIER_MODE;
import static io.gravitee.inference.api.Constants.CONFIG_JSON_PATH;
import static io.gravitee.inference.api.Constants.INFERENCE_FORMAT;
import static io.gravitee.inference.api.Constants.INFERENCE_TYPE;
import static io.gravitee.inference.api.Constants.INPUT;
import static io.gravitee.inference.api.Constants.MODEL_PATH;
import static io.gravitee.inference.api.Constants.TOKENIZER_PATH;

import io.gravitee.inference.api.Constants;
import io.gravitee.inference.api.classifier.ClassifierMode;
import io.gravitee.inference.api.classifier.ClassifierResults;
import io.gravitee.inference.api.service.InferenceAction;
import io.gravitee.inference.api.service.InferenceFormat;
import io.gravitee.inference.api.service.InferenceRequest;
import io.gravitee.inference.api.service.InferenceType;
import io.gravitee.resource.ai_model.model.ModelFileType;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.eventbus.Message;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InferenceServiceClient {

    private final Vertx vertx;

    private String modelAddress;

    public InferenceServiceClient(Vertx vertx) {
        this.vertx = vertx;
    }

    public Single<ClassifierResults> inferModel(String prompt, Map<ModelFileType, String> modelFiles) {
        return getModelAddress(modelFiles)
            .flatMap(address ->
                vertx
                    .eventBus()
                    .<Buffer>request(modelAddress, Json.encodeToBuffer(infer(prompt)))
                    .map(message -> Json.decodeValue(message.body(), ClassifierResults.class))
            )
            .doOnError(throwable -> log.error(throwable.getMessage(), throwable));
    }

    private Single<String> getModelAddress(Map<ModelFileType, String> modelFiles) {
        if (modelAddress == null) {
            return loadModel(modelFiles).map(message -> message.body().toString());
        }

        return Single.just(modelAddress);
    }

    private Single<Message<Buffer>> loadModel(Map<ModelFileType, String> modelFiles) {
        return vertx
            .eventBus()
            .<Buffer>request(Constants.SERVICE_INFERENCE_MODELS_ADDRESS, Json.encodeToBuffer(startModel(modelFiles)))
            .doOnSuccess(bufferMessage -> this.modelAddress = bufferMessage.body().toString())
            .doOnError(throwable -> log.error(throwable.getMessage(), throwable));
    }

    private InferenceRequest startModel(Map<ModelFileType, String> modelFiles) {
        log.debug("Starting model with files:{}", modelFiles);
        return new InferenceRequest(
            InferenceAction.START,
            Map.of(
                INFERENCE_FORMAT,
                InferenceFormat.ONNX_BERT,
                INFERENCE_TYPE,
                InferenceType.CLASSIFIER,
                CLASSIFIER_MODE,
                ClassifierMode.SEQUENCE,
                MODEL_PATH,
                modelFiles.get(ModelFileType.MODEL),
                TOKENIZER_PATH,
                modelFiles.get(ModelFileType.TOKENIZER),
                CONFIG_JSON_PATH,
                modelFiles.get(ModelFileType.CONFIG)
            )
        );
    }

    private static InferenceRequest infer(String sentence) {
        return new InferenceRequest(InferenceAction.INFER, Map.of(INPUT, sentence));
    }
}
