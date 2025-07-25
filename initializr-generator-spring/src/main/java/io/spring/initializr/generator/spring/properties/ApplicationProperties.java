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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Application properties.
 *
 * @author Moritz Halbritter
 */
public class ApplicationProperties {

	private static final String YAML_SPACE = "  ";

	private final Map<String, Object> properties = new HashMap<>();

	/**
	 * Adds a new property.
	 * @param key the key of the property
	 * @param value the value of the property
	 */
	public void add(String key, long value) {
		add(key, (Object) value);
	}

	/**
	 * Adds a new property.
	 * @param key the key of the property
	 * @param value the value of the property
	 */
	public void add(String key, boolean value) {
		add(key, (Object) value);
	}

	/**
	 * Adds a new property.
	 * @param key the key of the property
	 * @param value the value of the property
	 */
	public void add(String key, double value) {
		add(key, (Object) value);
	}

	/**
	 * Adds a new property.
	 * @param key the key of the property
	 * @param value the value of the property
	 */
	public void add(String key, String value) {
		add(key, (Object) value);
	}

	void writeTo(PrintWriter writer) {
		for (Map.Entry<String, Object> entry : this.properties.entrySet()) {
			writer.printf("%s=%s%n", entry.getKey(), entry.getValue());
		}
	}

	void writeToYaml(PrintWriter writer) {
		Map<String, Object> nested = new HashMap<>();
		flattenToNestedMap(this.properties, nested);
		writeYaml(nested, writer);
	}

	private void flattenToNestedMap(Map<String, Object> flatMap, Map<String, Object> nestedMap) {
		for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
			String[] path = parseKeyPath(entry.getKey());
			insertValueAtPath(nestedMap, path, entry.getValue());
		}
	}

	private String[] parseKeyPath(String key) {
		return key.split("\\.");
	}

	@SuppressWarnings("unchecked")
	private void insertValueAtPath(Map<String, Object> map, String[] path, Object value) {
		Map<String, Object> current = map;
		for (int i = 0; i < path.length - 1; i++) {
			String segment = path[i];
			current = (Map<String, Object>) current.computeIfAbsent(segment, (k) -> new HashMap<>());
		}
		current.put(path[path.length - 1], value);
	}

	private void add(String key, Object value) {
		Assert.state(!this.properties.containsKey(key), () -> "Property '%s' already exists".formatted(key));
		this.properties.put(key, value);
	}

	private void writeYaml(Map<String, Object> map, PrintWriter writer) {
		writeYamlRecursive(map, writer, 0);
	}

	private void writeYamlRecursive(Map<String, Object> map, PrintWriter writer, int indent) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			writeEntry(entry, writer, indent);
		}
	}

	@SuppressWarnings("unchecked")
	private void writeEntry(Map.Entry<String, Object> entry, PrintWriter writer, int indent) {
		String indentStr = getIndent(indent);
		Object value = entry.getValue();

		if (value instanceof Map<?, ?> nestedMap) {
			writer.printf("%s%s:%n", indentStr, entry.getKey());
			writeYamlRecursive((Map<String, Object>) nestedMap, writer, indent + 1);
		}
		else {
			writer.printf("%s%s: %s%n", indentStr, entry.getKey(), value);
		}
	}

	private String getIndent(int level) {
		return YAML_SPACE.repeat(level);
	}

}
