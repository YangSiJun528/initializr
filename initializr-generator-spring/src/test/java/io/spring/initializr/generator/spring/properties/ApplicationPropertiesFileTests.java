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

package io.spring.initializr.generator.spring.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link ApplicationPropertiesFile}.
 *
 * @author Moritz Halbritter
 * @author Rodrigo Mibielli Peixoto
 */
class ApplicationPropertiesFileTests {

	@Test
	void shouldGetExistingKey() {
		ApplicationPropertiesFile file = new ApplicationPropertiesFile();
		file.add("test", "123");
		Object value = file.get("test");
		assertThat(value).isEqualTo("123");
	}

	@Test
	void shouldGetNullForMissingKey() {
		ApplicationPropertiesFile file = new ApplicationPropertiesFile();
		file.add("test", "123");
		Object value = file.get("test2");
		assertThat(value).isNull();
	}

	@Test
	void shouldGetExistingKeyWithCast() {
		ApplicationPropertiesFile file = new ApplicationPropertiesFile();
		file.add("test", 123L);
		Long value = file.get("test", Long.class);
		assertThat(value).isEqualTo(123L);
	}

	@Test
	void shouldGetNullForMissingKeyWithCast() {
		ApplicationPropertiesFile file = new ApplicationPropertiesFile();
		file.add("test", 123.4);
		Double value = file.get("test2", Double.class);
		assertThat(value).isNull();
	}

	@Test
	void shouldFailOnWrongCast() {
		ApplicationPropertiesFile file = new ApplicationPropertiesFile();
		file.add("test", 123L);
		assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> file.get("test", Integer.class));
	}

	@Test
	void shouldContainExistingKey() {
		ApplicationPropertiesFile file = new ApplicationPropertiesFile();
		file.add("test", "value");
		assertThat(file.contains("test")).isTrue();
	}

	@Test
	void shouldNotContainMissingKey() {
		ApplicationPropertiesFile file = new ApplicationPropertiesFile();
		file.add("test", "value");
		assertThat(file.contains("not-found")).isFalse();
	}

	@Test
	void shouldRemoveExistingKey() {
		ApplicationPropertiesFile file = new ApplicationPropertiesFile();
		file.add("test", "value");
		assertThat(file.remove("test")).isTrue();
		assertThat(file.contains("test")).isFalse();
	}

	@Test
	void shouldNotRemoveMissingKey() {
		ApplicationPropertiesFile file = new ApplicationPropertiesFile();
		file.add("test", "value");
		assertThat(file.remove("not-found")).isFalse();
		assertThat(file.contains("test")).isTrue();
	}

	@Test
	void shouldFailOnExistingProperty() {
		ApplicationPropertiesFile file = new ApplicationPropertiesFile();
		file.add("test", 1);
		assertThatIllegalStateException().isThrownBy(() -> file.add("test", 2))
			.withMessage("Property 'test' already exists");
	}

}
