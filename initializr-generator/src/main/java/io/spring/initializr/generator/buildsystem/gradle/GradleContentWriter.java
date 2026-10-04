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

package io.spring.initializr.generator.buildsystem.gradle;

import io.spring.initializr.generator.buildsystem.content.BuildFragment;
import io.spring.initializr.generator.buildsystem.content.BuildValue;
import io.spring.initializr.generator.io.IndentingWriter;

/**
 * Renders build content using Groovy or Kotlin syntax.
 *
 * @author Sijun Yang
 */
final class GradleContentWriter {

	private GradleContentWriter() {
	}

	static String valueAsString(BuildValue value, char quote) {
		if (value.kind() == BuildValue.Kind.RAW) {
			return value.content();
		}
		StringBuilder result = new StringBuilder().append(quote);
		for (int i = 0; i < value.content().length(); i++) {
			char character = value.content().charAt(i);
			switch (character) {
				case '\\' -> result.append("\\\\");
				case '\n' -> result.append("\\n");
				case '\r' -> result.append("\\r");
				case '\t' -> result.append("\\t");
				case '\b' -> result.append("\\b");
				case '\f' -> result.append("\\u000c");
				default -> {
					if (character == quote || (quote == '"' && character == '$')) {
						result.append('\\');
					}
					result.append(character);
				}
			}
		}
		return result.append(quote).toString();
	}

	static void writeFragment(IndentingWriter writer, BuildFragment fragment) {
		if (fragment.kind() == BuildFragment.Kind.COMMENT) {
			for (String line : fragment.content().split("\\r\\n|\\r|\\n", -1)) {
				writer.println("// " + line);
			}
		}
		else {
			writer.println(fragment.content());
		}
	}

}
