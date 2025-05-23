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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.resource.ai_model.exception.ModelConfigLoadingException;
import io.gravitee.resource.ai_model.model.ModelConfiguration;
import io.gravitee.resource.ai_model.utils.ModelsConfigMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import lombok.Getter;

public class ModelsConfigurationLoader {

    @Getter
    private Map<String, ModelConfiguration> models;

    private final ObjectMapper mapper;

    public ModelsConfigurationLoader() {
        this.mapper = ModelsConfigMapper.mapper();
        this.models = loadModels();
    }

    public ModelConfiguration getModel(String modelId) {
        return models.get(modelId);
    }

    private Map<String, ModelConfiguration> loadModels() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("models-config.json")) {
            if (is == null) {
                throw new IllegalStateException("Cannot find model-presets.json in resources");
            }
            models = mapper.readValue(is, new TypeReference<>() {});
        } catch (IOException e) {
            throw new ModelConfigLoadingException("Failed to load models config", e);
        }
        return models;
    }
}
