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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.gravitee.inference.api.classifier.ClassifierResult;
import io.gravitee.inference.api.classifier.ClassifierResults;
import io.gravitee.resource.ai_model.api.ModelInvokeException;
import io.gravitee.resource.ai_model.api.model.ModelFileType;
import io.gravitee.resource.ai_model.api.model.PromptInput;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.buffer.Buffer;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.eventbus.EventBus;
import io.vertx.rxjava3.core.eventbus.Message;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

    @InjectMocks
    TextClassificationInferenceClient client;

    @BeforeEach
    void setup() {
        client = new TextClassificationInferenceClient(vertx);
    }

    @Test
    void shouldCallModelAddress() throws NoSuchFieldException, IllegalAccessException {
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

        Field field = client.getClass().getSuperclass().getDeclaredField("modelAddress");
        field.setAccessible(true);
        field.set(client, existingModelAddress);

        when(vertx.eventBus()).thenReturn(eventBus);

        when(eventBus.<Buffer>request(eq(existingModelAddress), any(Buffer.class))).thenReturn(Single.just(inferResponse));
        when(inferResponse.body()).thenReturn(Buffer.buffer(classifierJson));

        // when
        ClassifierResults result = client
            .infer(new PromptInput(prompt))
            .test()
            .awaitDone(5, TimeUnit.SECONDS)
            .assertComplete()
            .assertNoErrors()
            .values()
            .getFirst();

        // then
        assertNotNull(result);
        assertThat(result.results()).containsExactly(new ClassifierResult("neutral", 1.0f));
    }

    @Test
    void shouldThrowModelInvokeExceptionDueToMissingAddress() {
        // given
        String prompt = "Skip loading?";

        // when
        client.infer(new PromptInput(prompt)).test().awaitDone(5, TimeUnit.SECONDS).assertError(ModelInvokeException.class);

        // then
        verify(eventBus, never()).request(eq(SERVICE_INFERENCE_MODELS_ADDRESS), any());
    }

    @Test
    void shouldThrowErrorDueToBusError() throws NoSuchFieldException, IllegalAccessException {
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

        Field field = client.getClass().getSuperclass().getDeclaredField("modelAddress");
        field.setAccessible(true);
        field.set(client, existingModelAddress);

        //when
        when(vertx.eventBus()).thenReturn(eventBus);

        when(eventBus.<Buffer>request(eq(existingModelAddress), any(Buffer.class)))
            .thenReturn(Single.error(new RuntimeException("An error has occurred")));

        //then
        client.infer(new PromptInput(prompt)).test().awaitDone(5, TimeUnit.SECONDS).assertError(RuntimeException.class);
    }
}
