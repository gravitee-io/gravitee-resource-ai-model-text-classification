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

import static io.gravitee.inference.api.Constants.SERVICE_INFERENCE_MODELS_ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.gravitee.inference.api.classifier.ClassifierResult;
import io.gravitee.inference.api.classifier.ClassifierResults;
import io.gravitee.inference.service.InferenceService;
import io.gravitee.resource.ai_model.model.ModelFileType;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.buffer.Buffer;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.eventbus.EventBus;
import io.vertx.rxjava3.core.eventbus.Message;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InferenceServiceClientTest {

    @Mock
    Vertx vertx;

    @Mock
    EventBus eventBus;

    @Mock
    Message<Buffer> startModelResponse;

    @Mock
    Message<Buffer> inferResponse;

    @Mock
    InferenceService inferenceService;

    @InjectMocks
    InferenceServiceClient client;

    @BeforeEach
    void setup() {
        client = new InferenceServiceClient(vertx, inferenceService);
    }

    @Test
    void shouldLoadModelIfAddressIsNullAndThenInfer() {
        // given
        String modelAddress = "model::123";
        String classifierJson =
            """
            {
              "results": [
                {
                  "label": "toxic",
                  "score": 0.95
                },
                {
                  "label": "obscene",
                  "score": 0.05
                }
              ]
            }
            """;
        String prompt = "Very inappropriate prompt";
        Map<ModelFileType, String> modelFiles = Map.of(
            ModelFileType.MODEL,
            "model.onnx",
            ModelFileType.TOKENIZER,
            "tokenizer.json",
            ModelFileType.CONFIG,
            "config.json"
        );

        //when
        when(vertx.eventBus()).thenReturn(eventBus);

        when(eventBus.<Buffer>request(eq(SERVICE_INFERENCE_MODELS_ADDRESS), any())).thenReturn(Single.just(startModelResponse));
        when(startModelResponse.body()).thenReturn(Buffer.buffer(modelAddress));

        when(eventBus.<Buffer>request(eq(modelAddress), any())).thenReturn(Single.just(inferResponse));
        when(inferResponse.body()).thenReturn(Buffer.buffer(classifierJson));

        //then
        ClassifierResults result = client.inferModel(prompt, modelFiles).blockingGet();

        assertThat(result).isNotNull();
        assertThat(result.results()).containsExactly(new ClassifierResult("toxic", 0.95f), new ClassifierResult("obscene", 0.05f));
    }

    @Test
    void shouldUseCachedModelAddressAndSkipModelLoading() throws NoSuchFieldException, IllegalAccessException {
        // given
        String existingModelAddress = "cached::model::addr";
        String prompt = "Skip loading?";
        String classifierJson =
            """
        {
          "results": [
            { "label": "neutral", "score": 1.0 }
          ]
        }
        """;

        Field field = InferenceServiceClient.class.getDeclaredField("modelAddress");
        field.setAccessible(true);
        field.set(client, existingModelAddress);

        when(vertx.eventBus()).thenReturn(eventBus);

        when(eventBus.<Buffer>request(eq(existingModelAddress), any(Buffer.class))).thenReturn(Single.just(inferResponse));
        when(inferResponse.body()).thenReturn(Buffer.buffer(classifierJson));

        // when
        ClassifierResults result = client.inferModel(prompt, Map.of()).blockingGet();

        // then
        assertNotNull(result);
        assertThat(result.results()).containsExactly(new ClassifierResult("neutral", 1.0f));
        verify(eventBus, never()).request(eq(SERVICE_INFERENCE_MODELS_ADDRESS), any());
    }

    @Test
    void shouldPropagateErrorIfEventBusFails() {
        //given
        String prompt = "fail";
        Map<ModelFileType, String> modelFiles = Map.of(
            ModelFileType.MODEL,
            "model.onnx",
            ModelFileType.TOKENIZER,
            "tokenizer.json",
            ModelFileType.CONFIG,
            "config.json"
        );

        //when
        when(vertx.eventBus()).thenReturn(eventBus);

        when(eventBus.<Buffer>request(eq(SERVICE_INFERENCE_MODELS_ADDRESS), any(Buffer.class)))
            .thenReturn(Single.error(new RuntimeException("Model loading failed")));

        //then
        client
            .inferModel(prompt, modelFiles)
            .test()
            .assertError(e -> e instanceof RuntimeException && e.getMessage().equals("Model loading failed"));
    }

    @Test
    void shouldInitializeService() throws Exception {
        client.initialize();
        verify(inferenceService).start();
    }

    @Test
    void shouldFailWhenStartThrowsException() throws Exception {
        doThrow(new RuntimeException("Inference service failed loading")).when(inferenceService).start();

        assertThatThrownBy(() -> client.initialize()).isInstanceOf(RuntimeException.class).hasMessage("Inference service failed loading");
    }
}
