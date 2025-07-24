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

		// 1. 점(.)을 기준으로 키 분해하여 중첩 맵 구성
		for (Map.Entry<String, Object> entry : this.properties.entrySet()) {
			insertNestedKey(nested, entry.getKey().split("\\."), 0, entry.getValue());
		}

		// 2. 재귀적으로 출력
		writeYamlRecursive(nested, writer, 0);
	}

	private void add(String key, Object value) {
		Assert.state(!this.properties.containsKey(key), () -> "Property '%s' already exists".formatted(key));
		this.properties.put(key, value);
	}

	// TODO: GPT가 생성한 코드, 다른 구현이나 스프링 내부의 다른 예시 보고 적절하게 수정하기
	@SuppressWarnings("unchecked")
	private void insertNestedKey(Map<String, Object> map, String[] keys, int index, Object value) {
		String key = keys[index];
		if (index == keys.length - 1) {
			map.put(key, value);
		}
		else {
			map.computeIfAbsent(key, k -> new HashMap<String, Object>());
			insertNestedKey((Map<String, Object>) map.get(key), keys, index + 1, value);
		}
	}

	private void writeYamlRecursive(Map<String, Object> map, PrintWriter writer, int indent) {
		String indentStr = "  ".repeat(indent);
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map<?, ?> nestedMap) {
				writer.printf("%s%s:%n", indentStr, entry.getKey());
				writeYamlRecursive((Map<String, Object>) nestedMap, writer, indent + 1);
			}
			else {
				writer.printf("%s%s: %s%n", indentStr, entry.getKey(), value);
			}
		}
	}

}
