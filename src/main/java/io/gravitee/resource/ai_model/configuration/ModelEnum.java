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
package io.gravitee.resource.ai_model.configuration;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ModelEnum {
    MINILMV2_TOXIC_JIGSAW_MODEL(
        "MINILMV2_TOXIC_JIGSAW_MODEL",
        "minuva/MiniLMv2-toxic-jigsaw-onnx",
        "model_optimized_quantized.onnx",
        "tokenizer.json",
        "config.json"
    ),
    GRAVITEE_DETOXIFY_ONNX_MODEL(
        "GRAVITEE_DETOXIFY_ONNX_MODEL",
        "gravitee-io/detoxify-onnx",
        "model.quant.onnx",
        "tokenizer.json",
        "config.json"
    ),
    GRAVITEE_IO_DISTILBERT_MULTILINGUAL_TOXICITY_CLASSIFIER(
        "GRAVITEE_IO_DISTILBERT_MULTILINGUAL_TOXICITY_CLASSIFIER",
        "gravitee-io/distilbert-multilingual-toxicity-classifier",
        "model.quant.onnx",
        "tokenizer.json",
        "config.json"
    ),
    GRAVITEE_LLAMA_PROMPT_GUARD_22M_MODEL(
        "LLAMA_PROMPT_GUARD_22M_MODEL",
        "gravitee-io/Llama-Prompt-Guard-2-22M-onnx",
        "model.quant.onnx",
        "tokenizer.json",
        "config.json"
    ),
    GRAVITEE_LLAMA_PROMPT_GUARD_86M_MODEL(
        "LLAMA_PROMPT_GUARD_86M_MODEL",
        "gravitee-io/Llama-Prompt-Guard-2-86M-onnx",
        "model.quant.onnx",
        "tokenizer.json",
        "config.json"
    );

    @JsonValue
    private final String type;

    private final String modelName;
    private final String modelFile;
    private final String tokenizerFile;
    private final String configFile;

    ModelEnum(String type, String modelName, String modelFile, String tokenizerFile, String configFile) {
        this.type = type;
        this.modelName = modelName;
        this.modelFile = modelFile;
        this.tokenizerFile = tokenizerFile;
        this.configFile = configFile;
    }
}
