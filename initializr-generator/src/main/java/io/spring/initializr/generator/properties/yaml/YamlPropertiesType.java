package io.spring.initializr.generator.properties.yaml;

import io.spring.initializr.generator.properties.PropertiesType;

public class YamlPropertiesType implements PropertiesType {

    /**
     * YAML {@link PropertiesType} identifier.
     */
    public static final String ID = "yaml";

    @Override
    public String id() {
        return ID;
    }

}
