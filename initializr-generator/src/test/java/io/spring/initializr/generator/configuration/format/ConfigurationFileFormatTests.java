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

package io.spring.initializr.generator.configuration.format;

import io.spring.initializr.generator.configuration.format.properties.PropertiesFormat;
import io.spring.initializr.generator.configuration.format.yaml.YamlFormat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link ConfigurationFileFormat}.
 *
 * @author Moritz Halbritter
 */
class ConfigurationFileFormatTests {

	@Test
	void propertiesFormat() {
		ConfigurationFileFormat format = ConfigurationFileFormat.forId(PropertiesFormat.ID);
		assertThat(format).isInstanceOf(PropertiesFormat.class);
		assertThat(format.id()).isEqualTo("properties");
		assertThat(format.fileExtension()).isEqualTo(".properties");
		assertThat(format).hasToString("properties");
	}

	@Test
	void yamlFormat() {
		ConfigurationFileFormat format = ConfigurationFileFormat.forId(YamlFormat.ID);
		assertThat(format).isInstanceOf(YamlFormat.class);
		assertThat(format.id()).isEqualTo("yaml");
		assertThat(format.fileExtension()).isEqualTo(".yaml");
		assertThat(format).hasToString("yaml");
	}

	@Test
	void unknownFormat() {
		assertThatIllegalStateException().isThrownBy(() -> ConfigurationFileFormat.forId("unknown"))
			.withMessageContaining("Unrecognized configuration file format id 'unknown'");
	}

}
