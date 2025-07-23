package io.spring.initializr.generator.properties.yaml;

import io.spring.initializr.generator.properties.PropertiesType;
import io.spring.initializr.generator.properties.PropertiesTypeFactory;

public class YamlPropertiesTypeFactory implements PropertiesTypeFactory {

	@Override
	public PropertiesType create(String id) {
		return YamlPropertiesType.ID.equals(id) ? new YamlPropertiesType() : null;
	}

}
