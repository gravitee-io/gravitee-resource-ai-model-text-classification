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

import io.micrometer.common.lang.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.buffer.Buffer;
import io.vertx.rxjava3.RxHelper;
import io.vertx.rxjava3.core.Vertx;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InferenceServiceClientLoadModelTest {

    public static final String SOME_ADDESS = "some::addess";

    TextClassificationInferenceClient client;

    Vertx vertx;

    @NonNull
    Disposable consumer;

    @BeforeEach
    void setup() {
        this.vertx = Vertx.vertx();
        client = new TextClassificationInferenceClient(vertx);
        consumer =
            this.vertx.eventBus()
                .consumer(SERVICE_INFERENCE_MODELS_ADDRESS)
                .toObservable()
                .subscribeOn(RxHelper.blockingScheduler(vertx.getDelegate()))
                .observeOn(RxHelper.blockingScheduler(vertx.getDelegate()))
                .subscribe(message -> {
                    Thread.sleep(10000);
                    message.reply(Buffer.buffer(SOME_ADDESS));
                });
    }

    @Test
    void must_load_model_with_latency() {
        client
            .loadModel(Map.of("some", "configuration"))
            .test()
            .awaitDone(20, TimeUnit.SECONDS)
            .assertComplete()
            .assertNoErrors()
            .assertValue(SOME_ADDESS::equals);
    }

    @AfterEach
    public void teardown() {
        consumer.dispose();
    }
}
