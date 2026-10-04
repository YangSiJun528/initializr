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

package io.spring.initializr.generator.buildsystem.maven;

import io.spring.initializr.generator.buildsystem.content.BuildFragment;
import io.spring.initializr.generator.buildsystem.content.BuildValue;
import io.spring.initializr.generator.io.IndentingWriter;

/**
 * Renders build content using XML syntax.
 *
 * @author Sijun Yang
 */
final class MavenContentWriter {

	private MavenContentWriter() {
	}

	static void writeValue(IndentingWriter writer, String name, BuildValue value) {
		if (value.content().isEmpty()) {
			writer.println("<%s/>".formatted(name));
		}
		else {
			writer.print("<%s>".formatted(name));
			writer.print((value.kind() == BuildValue.Kind.RAW) ? value.content() : encodeText(value.content()));
			writer.println("</%s>".formatted(name));
		}
	}

	static void writeFragment(IndentingWriter writer, BuildFragment fragment) {
		if (fragment.kind() == BuildFragment.Kind.COMMENT) {
			String comment = fragment.content();
			if (comment.contains("--") || comment.codePoints().anyMatch((character) -> !isXmlCharacter(character))) {
				throw new IllegalArgumentException(
						"Comment must contain valid XML characters and must not contain '--'");
			}
			writer.println("<!-- %s -->".formatted(comment));
		}
		else {
			writer.println(fragment.content());
		}
	}

	private static boolean isXmlCharacter(int character) {
		return character == 0x9 || character == 0xA || character == 0xD || (character >= 0x20 && character <= 0xD7FF)
				|| (character >= 0xE000 && character <= 0xFFFD) || (character >= 0x10000 && character <= 0x10FFFF);
	}

	private static String encodeText(String text) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char character = text.charAt(i);
			switch (character) {
				case '\'' -> result.append("&apos;");
				case '"' -> result.append("&quot;");
				case '<' -> result.append("&lt;");
				case '>' -> result.append("&gt;");
				case '&' -> result.append("&amp;");
				default -> result.append(character);
			}
		}
		return result.toString();
	}

}
