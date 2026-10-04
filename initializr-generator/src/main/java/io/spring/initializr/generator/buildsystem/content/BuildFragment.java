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
 * A standalone comment or raw fragment, separate from a scalar {@link BuildValue}. Raw
 * fragments must use the syntax of the target build writer. Writers may add surrounding
 * indentation and a newline, but preserve the fragment's internal whitespace.
 *
 * @param kind the kind of fragment
 * @param content the fragment content
 * @author Sijun Yang
 */
public record BuildFragment(Kind kind, String content) {

	public BuildFragment {
		Objects.requireNonNull(kind, "kind");
		Objects.requireNonNull(content, "content");
	}

	/**
	 * Create a comment without syntax-specific delimiters.
	 * @param content the comment text
	 * @return the fragment
	 */
	public static BuildFragment comment(String content) {
		return new BuildFragment(Kind.COMMENT, content);
	}

	/**
	 * Create a raw fragment in the target syntax, without escaping.
	 * @param content the raw content
	 * @return the fragment
	 */
	public static BuildFragment raw(String content) {
		return new BuildFragment(Kind.RAW, content);
	}

	public enum Kind {

		/** A comment rendered using the target syntax. */
		COMMENT,

		/** Content already expressed in the target syntax. */
		RAW

	}

}
