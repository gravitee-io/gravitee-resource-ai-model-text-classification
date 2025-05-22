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

import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.resource.ai_model.api.ClassifierResults;
import io.gravitee.resource.ai_model.api.model.PromptInput;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@ContextConfiguration(classes = { TestConfiguration.class })
class TextClassificationAiModelResourceIntegrationTest {

    @Autowired
    TextClassificationAiModelResource resource;

    @BeforeEach
    void setUp() throws Exception {
        resource.doStart();
    }

    @AfterEach
    void tearDown() throws Exception {
        resource.doStop();
    }

    @Test
    void shouldReturnExpectedClassificationForToxicSentence() {
        List<ClassifierResults.ClassifierResult> expected = List.of(
            new ClassifierResults.ClassifierResult("toxic", 0.64473337f, null, null, null),
            new ClassifierResults.ClassifierResult("obscene", 0.30525568f, null, null, null),
            new ClassifierResults.ClassifierResult("insult", 0.08316804f, null, null, null),
            new ClassifierResults.ClassifierResult("severe_toxic", 0.0032595915f, null, null, null),
            new ClassifierResults.ClassifierResult("identity_hate", 0.0026842426f, null, null, null),
            new ClassifierResults.ClassifierResult("threat", 0.0012624051f, null, null, null)
        );

        Comparator<Float> floatComparatorWithTolerance = (f1, f2) -> {
            if (f1 == null || f2 == null) return Comparator.nullsFirst(Float::compare).compare(f1, f2);
            return Math.abs(f1 - f2) < 0.0001f ? 0 : Float.compare(f1, f2);
        };

        resource
            .invokeModel(new PromptInput("Nobody asked for your bullsh*t response."))
            .test()
            .awaitDone(10, TimeUnit.SECONDS)
            .assertComplete()
            .assertNoErrors()
            .assertValue(actualResults -> {
                assertThat(actualResults.results())
                    .usingRecursiveComparison()
                    .withComparatorForType(floatComparatorWithTolerance, Float.class)
                    .isEqualTo(expected);
                return true;
            });
    }
}
