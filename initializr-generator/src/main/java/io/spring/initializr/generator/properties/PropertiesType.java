/*
 * Copyright 2012 - present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.generator.properties;

import java.util.Objects;

import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Application properties type, such as a properties file or a yaml file.
 *
 * @author Bondit Manager
 */
public interface PropertiesType {

    /**
     * Return the id of the properties type.
     * @return the id
     */
    String id();

    /**
     * Creates the properties type for the given id.
     * @param id the id
     * @return the properties type
     * @throws IllegalStateException if the properties type with the given id can't be found
     */
    static PropertiesType forId(String id) {
        return SpringFactoriesLoader.loadFactories(PropertiesTypeFactory.class, PropertiesType.class.getClassLoader())
                .stream()
                .map((factory) -> factory.create(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unrecognized properties type id '" + id + "'"));
    }

}
