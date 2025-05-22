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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.gravitee.resource.ai_model.exception.ModelDownloadFailedException;
import io.gravitee.resource.ai_model.exception.ModelFileNotFoundException;
import io.gravitee.resource.ai_model.fetcher.VertxHuggingFaceClientRx;
import io.gravitee.resource.ai_model.model.ModelFile;
import io.gravitee.resource.ai_model.model.ModelFileType;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.AsyncFile;
import io.vertx.rxjava3.core.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class HuggingFaceDownloaderServiceTest {

    @Test
    void shouldSkipDownloadWhenFileExistsLocally() {
        String modelName = "test-model";
        ModelFile file = new ModelFile("config.json", ModelFileType.CONFIG);
        Path modelDir = Path.of("temp-model-dir");

        var vertx = mock(Vertx.class);
        var fileSystem = mock(FileSystem.class);
        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.rxExists(modelDir.resolve(file.name()).toString())).thenReturn(Single.just(true));

        var client = mock(VertxHuggingFaceClientRx.class);
        when(client.listModelFiles(modelName)).thenReturn(Flowable.just("config.json"));

        var service = new HuggingFaceDownloaderService(vertx, client);

        service
            .downloadModel(modelName, List.of(file), modelDir)
            .test()
            .awaitDone(5, TimeUnit.SECONDS)
            .assertComplete()
            .assertNoErrors()
            .assertValue(result -> {
                assertThat(result.get(ModelFileType.CONFIG)).contains("config.json");
                return true;
            });

        verify(client, never()).downloadModelFile(any(), any(), any());
    }

    @Test
    void shouldDownloadFileWhenNotExistsLocally() {
        String modelName = "test-model";
        ModelFile file = new ModelFile("config.json", ModelFileType.CONFIG);
        Path modelDir = Path.of("temp-model-dir");

        var vertx = mock(Vertx.class);
        var fileSystem = mock(FileSystem.class);
        var asyncFile = mock(AsyncFile.class);

        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.rxExists(modelDir.resolve(file.name()).toString())).thenReturn(Single.just(false));
        when(fileSystem.rxOpen(eq(modelDir.resolve(file.name()).toString()), any())).thenReturn(Single.just(asyncFile));

        var client = mock(VertxHuggingFaceClientRx.class);
        when(client.listModelFiles(modelName)).thenReturn(Flowable.just(file.name()));
        when(client.downloadModelFile(modelName, file.name(), asyncFile)).thenReturn(Completable.complete());

        var service = new HuggingFaceDownloaderService(vertx, client);

        service
            .downloadModel(modelName, List.of(file), modelDir)
            .test()
            .awaitDone(5, TimeUnit.SECONDS)
            .assertComplete()
            .assertNoErrors()
            .assertValue(result -> {
                assertThat(result.get(ModelFileType.CONFIG)).contains("temp-model-dir/config.json");
                return true;
            });
        verify(client).downloadModelFile(modelName, file.name(), asyncFile);
        verify(asyncFile).close();
    }

    @Test
    void shouldThrowWhenFileNotAvailableRemotely() {
        String modelName = "test-model";
        ModelFile file = new ModelFile("missing.json", ModelFileType.MODEL);
        Path modelDir = Path.of("temp-model-dir");

        var client = mock(VertxHuggingFaceClientRx.class);
        when(client.listModelFiles(modelName)).thenReturn(Flowable.just("other_file.json"));

        var service = new HuggingFaceDownloaderService(mock(Vertx.class), client);

        service.downloadModel(modelName, List.of(file), modelDir).test().assertError(ModelFileNotFoundException.class);
    }

    @Test
    void shouldThrowWhenDownloadFails() {
        String modelName = "test-model";
        ModelFile file = new ModelFile("model.onnx", ModelFileType.MODEL);
        Path modelDir = Path.of("temp-model-dir");

        var vertx = mock(Vertx.class);
        var fileSystem = mock(FileSystem.class);
        var asyncFile = mock(AsyncFile.class);

        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.rxExists(any())).thenReturn(Single.just(false));
        when(fileSystem.rxOpen(any(), any())).thenReturn(Single.just(asyncFile));
        when(asyncFile.close()).thenReturn(Completable.complete());

        var client = mock(VertxHuggingFaceClientRx.class);
        when(client.listModelFiles(modelName)).thenReturn(Flowable.just("model.onnx"));
        when(client.downloadModelFile(modelName, "model.onnx", asyncFile))
            .thenReturn(Completable.error(new RuntimeException("network timeout")));

        var service = new HuggingFaceDownloaderService(vertx, client);

        service.downloadModel(modelName, List.of(file), modelDir).test().assertError(ModelDownloadFailedException.class);
    }
}
