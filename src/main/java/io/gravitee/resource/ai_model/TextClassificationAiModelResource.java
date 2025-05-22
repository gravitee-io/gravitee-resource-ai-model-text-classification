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
import io.gravitee.resource.ai_model.api.model.PromptInput;
import io.gravitee.resource.ai_model.client.InferenceServiceClient;
import io.gravitee.resource.ai_model.configuration.TextClassificationAiModelConfiguration;
import io.gravitee.resource.ai_model.downloader.HuggingFaceDownloaderService;
import io.gravitee.resource.ai_model.downloader.HuggingFaceWebClientFactory;
import io.gravitee.resource.ai_model.fetcher.VertxHuggingFaceClientRx;
import io.gravitee.resource.ai_model.model.ModelFile;
import io.gravitee.resource.ai_model.model.ModelFileType;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final String GRAVITEE_HOME = "gravitee.home";
    private static final String GRAVITEE_HOME_PATH = System.getProperty(GRAVITEE_HOME);

    private String modelId;
    private List<ModelFile> modelFiles;
    private Path modelDirectory;

    private ApplicationContext applicationContext;
    private HuggingFaceDownloaderService huggingFaceDownloaderService;
    private InferenceServiceClient inferenceServiceClient;

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        this.modelId = configuration().model().modelName();
        this.modelFiles = getModelFiles();
        this.modelDirectory = getFileDirectory();

        Vertx vertx = applicationContext.getBean(Vertx.class);
        this.inferenceServiceClient = new InferenceServiceClient(vertx);

        var huggingFaceWebClient = HuggingFaceWebClientFactory.createDefaultClient(vertx);
        var vertxHuggingFaceClientRx = new VertxHuggingFaceClientRx(huggingFaceWebClient);
        this.huggingFaceDownloaderService = new HuggingFaceDownloaderService(vertx, vertxHuggingFaceClientRx);
    }

    private List<ModelFile> getModelFiles() {
        var modelConfig = configuration().model();
        return List.of(
            new ModelFile(modelConfig.modelFile(), ModelFileType.MODEL),
            new ModelFile(modelConfig.tokenizerFile(), ModelFileType.TOKENIZER),
            new ModelFile(modelConfig.configFile(), ModelFileType.CONFIG)
        );
    }

    private Path getFileDirectory() throws IOException {
        Path directory = Path.of(GRAVITEE_HOME_PATH + "/models/" + modelId);
        try {
            return Files.createDirectories(directory);
        } catch (FileAlreadyExistsException faee) {
            log.debug("{} already exists, skip directory creation", directory);
            return directory;
        } catch (Exception e) {
            log.warn("Failed to create models directory, creating temp directory", e);
            return Files.createTempDirectory(modelId.replace("/", "-"));
        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        inferenceServiceClient.stopModel().blockingAwait();
    }

    @Override
    public Single<ClassifierResults> invokeModel(PromptInput promptInput) {
        return huggingFaceDownloaderService
            .downloadModel(modelId, modelFiles, modelDirectory)
            .flatMap(downloadedFiles -> {
                log.debug("Downloaded files to invoke the model: {}", downloadedFiles);
                return inferenceServiceClient
                    .inferModel(promptInput.promptContent(), downloadedFiles)
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
