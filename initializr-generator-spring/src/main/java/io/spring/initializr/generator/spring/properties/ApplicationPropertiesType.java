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

import java.util.Arrays;
import java.util.Locale;

import org.springframework.util.Assert;

/**
 * Defines the available application properties formats.
 *
 * @author Bondit Manager
 */
public enum ApplicationPropertiesType {

	/**
	 * Properties format.
	 */
	PROPERTIES(ID.PROPERTIES),

	/**
	 * YAML format.
	 */
	YAML(ID.YAML);

	private final String id;

	ApplicationPropertiesType(String id) {
		this.id = id;
	}

	/**
	 * Returns the {@link ApplicationPropertiesType} for the given id.
	 * @param id the format identifier
	 * @return the corresponding format
	 */
	public static ApplicationPropertiesType forId(String id) {
		Assert.notNull(id, "Id must not be null");
		String lowerCaseId = id.toLowerCase(Locale.ROOT);
		return Arrays.stream(values())
			.filter((format) -> format.id.equals(lowerCaseId))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unknown application properties format '" + id + "'"));
	}

	@Override
	public String toString() {
		return this.id;
	}

	/**
	 * Constant identifiers for the available formats.
	 */
	public static final class ID {

		/**
		 * Properties format ID.
		 */
		public static final String PROPERTIES = "properties";

		/**
		 * YAML format ID.
		 */
		public static final String YAML = "yaml";

	}

}
