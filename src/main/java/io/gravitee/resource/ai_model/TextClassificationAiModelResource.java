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

import static io.gravitee.inference.api.Constants.*;
import static io.gravitee.inference.api.Constants.CONFIG_JSON_PATH;

import io.gravitee.huggingface.reactive.webclient.downloader.HuggingFaceDownloader;
import io.gravitee.huggingface.reactive.webclient.downloader.HuggingFaceDownloader.FetchModelConfig;
import io.gravitee.inference.api.classifier.ClassifierMode;
import io.gravitee.inference.api.service.InferenceFormat;
import io.gravitee.inference.api.service.InferenceType;
import io.gravitee.resource.ai_model.api.AiTextModelResource;
import io.gravitee.resource.ai_model.api.ModelFetcher;
import io.gravitee.resource.ai_model.api.model.ModelFile;
import io.gravitee.resource.ai_model.api.model.ModelFileType;
import io.gravitee.resource.ai_model.api.model.PromptInput;
import io.gravitee.resource.ai_model.api.result.ClassifierResults;
import io.gravitee.resource.ai_model.client.TextClassificationInferenceClient;
import io.gravitee.resource.ai_model.configuration.TextClassificationAiModelConfiguration;
import io.reactivex.rxjava3.core.Single;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class TextClassificationAiModelResource
    extends AiTextModelResource<TextClassificationAiModelConfiguration, io.gravitee.inference.api.classifier.ClassifierResults, ClassifierResults> {

    private String modelId;
    private List<ModelFile> modelFiles;
    private Path modelDirectory;

    @Override
    protected void doStart() throws Exception {
        this.modelId = configuration().model().modelName();
        this.modelFiles = getModelFiles();
        this.modelDirectory = getFileDirectory();

        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
    }

    private List<ModelFile> getModelFiles() {
        var modelConfig = configuration().model();
        return List.of(
            new ModelFile(modelConfig.modelFile(), ModelFileType.MODEL),
            new ModelFile(modelConfig.tokenizerFile(), ModelFileType.TOKENIZER),
            new ModelFile(modelConfig.configFile(), ModelFileType.CONFIG)
        );
    }

    @Override
    public Single<ClassifierResults> invokeModel(PromptInput promptInput) {
        return inferenceServiceClient.infer(promptInput).map(TextClassificationAiModelResource::mapToClassifierResults);
    }

    @Override
    protected ModelFetcher buildModelFetcher() {
        var config = new FetchModelConfig(modelId, modelFiles, modelDirectory);
        return new HuggingFaceDownloader(vertx, config);
    }

    @Override
    protected TextClassificationInferenceClient buildInferenceServiceClient() {
        return new TextClassificationInferenceClient(vertx);
    }

    @Override
    protected Map<String, Object> getModelConfiguration(Map<ModelFileType, String> modelFileMap) {
        return Map.of(
            INFERENCE_FORMAT,
            InferenceFormat.ONNX_BERT,
            INFERENCE_TYPE,
            InferenceType.CLASSIFIER,
            CLASSIFIER_MODE,
            ClassifierMode.SEQUENCE,
            MODEL_PATH,
            modelFileMap.get(ModelFileType.MODEL),
            TOKENIZER_PATH,
            modelFileMap.get(ModelFileType.TOKENIZER),
            CONFIG_JSON_PATH,
            modelFileMap.get(ModelFileType.CONFIG)
        );
    }

    @Override
    protected String getModelId() {
        return this.modelId;
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
