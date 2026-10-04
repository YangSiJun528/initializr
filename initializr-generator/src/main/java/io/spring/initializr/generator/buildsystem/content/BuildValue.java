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

package io.spring.initializr.generator.buildsystem.content;

import java.util.Objects;

/**
 * A scalar build value. Text is escaped or quoted by the target writer; raw content is
 * written without escaping and must already use the target syntax.
 *
 * @param kind the kind of value
 * @param content the value content
 * @author Sijun Yang
 */
public record BuildValue(Kind kind, String content) {

	public BuildValue {
		Objects.requireNonNull(kind, "kind");
		Objects.requireNonNull(content, "content");
	}

	/**
	 * Create a text value that the writer will escape or quote.
	 * @param content the text
	 * @return the value
	 */
	public static BuildValue text(String content) {
		return new BuildValue(Kind.TEXT, content);
	}

	/**
	 * Create a value in the target build syntax, without escaping.
	 * @param content the raw content
	 * @return the value
	 */
	public static BuildValue raw(String content) {
		return new BuildValue(Kind.RAW, content);
	}

	public enum Kind {

		/** Text that requires escaping or quoting. */
		TEXT,

		/** Content already expressed in the target syntax. */
		RAW

	}

}
