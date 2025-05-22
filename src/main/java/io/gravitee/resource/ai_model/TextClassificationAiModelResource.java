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

import io.gravitee.resource.ai_model.api.AiTextClassificationModelResource;
import io.gravitee.resource.ai_model.api.ClassifierResults;
import io.gravitee.resource.ai_model.client.InferenceServiceClient;
import io.gravitee.resource.ai_model.configuration.ModelsConfigurationLoader;
import io.gravitee.resource.ai_model.configuration.TextClassificationAiModelConfiguration;
import io.gravitee.resource.ai_model.downloader.HuggingFaceDownloaderService;
import io.gravitee.resource.ai_model.model.ModelFile;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.core.Vertx;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
public class TextClassificationAiModelResource
    extends AiTextClassificationModelResource<TextClassificationAiModelConfiguration>
    implements ApplicationContextAware {

    private String modelId;
    private List<ModelFile> modelFiles;

    private ApplicationContext applicationContext;
    private InferenceServiceClient inferenceServiceClient;
    private HuggingFaceDownloaderService huggingFaceDownloaderService;
    private Vertx vertx;

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        this.modelId = configuration().getModel().modelId();
        this.modelFiles = getModelFiles(modelId);

        this.vertx = applicationContext.getBean(Vertx.class);
        this.inferenceServiceClient = new InferenceServiceClient(vertx);
        this.huggingFaceDownloaderService = new HuggingFaceDownloaderService(vertx, modelId);
    }

    private List<ModelFile> getModelFiles(String modelId) {
        var modelConfigLoader = new ModelsConfigurationLoader();
        return modelConfigLoader.getModel(modelId).files();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        modelFiles.forEach(fileEntry ->
            vertx
                .fileSystem()
                .rxDelete(fileEntry.name())
                .doOnComplete(() -> log.info("File {} deleted", fileEntry.name()))
                .doOnError(throwable -> log.error(throwable.getMessage(), throwable))
                .blockingAwait()
        );
    }

    @Override
    public Maybe<ClassifierResults> invokeModel(String prompt) {
        return huggingFaceDownloaderService
            .downloadModel(modelFiles)
            .flatMap(downloadedFiles -> {
                log.info("Downloaded files to invoke the model: {}", downloadedFiles);
                return inferenceServiceClient
                    .inferModel(prompt, downloadedFiles)
                    .toMaybe()
                    .map(TextClassificationAiModelResource::mapToClassifierResults);
            });
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
