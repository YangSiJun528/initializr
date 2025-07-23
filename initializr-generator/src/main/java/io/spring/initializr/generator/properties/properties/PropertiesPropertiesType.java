package io.spring.initializr.generator.properties.properties;

import io.spring.initializr.generator.properties.PropertiesType;

public class PropertiesPropertiesType implements PropertiesType {

    /**
     * Properties {@link PropertiesType} identifier.
     */
    public static final String ID = "properties";

	@Override
	public String id() {
		return ID;
	}

}
