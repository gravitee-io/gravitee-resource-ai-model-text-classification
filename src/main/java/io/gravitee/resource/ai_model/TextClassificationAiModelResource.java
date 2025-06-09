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
package io.gravitee.resource.ai_model;

import static io.gravitee.inference.api.Constants.CLASSIFIER_MODE;
import static io.gravitee.inference.api.Constants.CONFIG_JSON_PATH;
import static io.gravitee.inference.api.Constants.INFERENCE_FORMAT;
import static io.gravitee.inference.api.Constants.INFERENCE_TYPE;
import static io.gravitee.inference.api.Constants.MODEL_PATH;
import static io.gravitee.inference.api.Constants.TOKENIZER_PATH;

import io.gravitee.inference.api.classifier.ClassifierMode;
import io.gravitee.inference.api.service.InferenceFormat;
import io.gravitee.inference.api.service.InferenceType;
import io.gravitee.resource.ai_model.api.AiTextModelResource;
import io.gravitee.resource.ai_model.api.model.PromptInput;
import io.gravitee.resource.ai_model.api.result.ClassifierResults;
import io.gravitee.resource.ai_model.client.TextClassificationInferenceClient;
import io.gravitee.resource.ai_model.configuration.TextClassificationAiModelConfiguration;
import io.reactivex.rxjava3.core.Single;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextClassificationAiModelResource
    extends AiTextModelResource<TextClassificationAiModelConfiguration, io.gravitee.inference.api.classifier.ClassifierResults, ClassifierResults> {

    private static final String MODEL_NAME = "modelName";

    @Override
    public Single<ClassifierResults> invokeModel(PromptInput promptInput) {
        return inferenceServiceClient.infer(promptInput).map(TextClassificationAiModelResource::mapToClassifierResults);
    }

    @Override
    protected String getModelName() {
        return configuration().model().modelName();
    }

    protected TextClassificationInferenceClient buildInferenceServiceClient() {
        return new TextClassificationInferenceClient(
            vertx,
            Map.ofEntries(
                Map.entry(MODEL_NAME, getModelName()),
                Map.entry(INFERENCE_FORMAT, InferenceFormat.ONNX_BERT),
                Map.entry(INFERENCE_TYPE, InferenceType.CLASSIFIER),
                Map.entry(CLASSIFIER_MODE, ClassifierMode.SEQUENCE),
                Map.entry(MODEL_PATH, configuration().model().modelFile()),
                Map.entry(TOKENIZER_PATH, configuration().model().tokenizerFile()),
                Map.entry(CONFIG_JSON_PATH, configuration().model().configFile())
            )
        );
    }

    private static ClassifierResults mapToClassifierResults(io.gravitee.inference.api.classifier.ClassifierResults classifierResults) {
        var results = classifierResults
            .results()
            .stream()
            .map(result ->
                new ClassifierResults.ClassifierResult(result.label(), result.score(), result.token(), result.start(), result.end())
            )
            .toList();
        return new ClassifierResults(results);
    }
}
