package io.spring.initializr.generator.properties.properties;

import io.spring.initializr.generator.properties.PropertiesType;
import io.spring.initializr.generator.properties.PropertiesTypeFactory;

public class PropertiesPropertiesTypeFactory implements PropertiesTypeFactory {

	@Override
	public PropertiesType create(String id) {
		return PropertiesPropertiesType.ID.equals(id) ? new PropertiesPropertiesType() : null;
	}

}
