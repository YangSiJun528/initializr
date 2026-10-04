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

import java.util.function.BiPredicate;

import io.spring.initializr.generator.io.IndentingWriter;

/**
 * Syntax-specific rendering of shared build content. Structured elements are rendered by
 * the owning build writer; raw content and callbacks use the target syntax.
 *
 * @author Sijun Yang
 */
public interface ContentWriter {

	/**
	 * Render a comment on a single line, including its delimiters.
	 * @param text the comment text
	 * @return the rendered comment
	 */
	String inlineComment(String text);

	/**
	 * Write a standalone fragment, including its terminating newline.
	 * @param writer the writer
	 * @param fragment the fragment
	 */
	void writeFragment(IndentingWriter writer, BuildFragment fragment);

	/**
	 * Write a sequence. The element writer must leave the final line unterminated so this
	 * method can append inline comments and the terminating newline. It returns whether
	 * an element was written; omitted elements cannot have inline comments. Callbacks
	 * manage their own newlines and indentation, just like standalone writer snippets.
	 * @param writer the writer
	 * @param sequence the sequence
	 * @param elementWriter the renderer for structured elements
	 * @param <T> the structured element type
	 */
	default <T> void write(IndentingWriter writer, ContentSequence<T> sequence,
			BiPredicate<IndentingWriter, T> elementWriter) {
		for (ContentSequence.Entry<T> entry : sequence.entries()) {
			if (entry instanceof ContentSequence.Element<T> element) {
				if (!elementWriter.test(writer, element.value())) {
					if (!element.inlineComments().isEmpty()) {
						throw new IllegalArgumentException(
								"Cannot attach an inline comment to omitted element '" + element.key() + "'");
					}
					continue;
				}
				for (String comment : element.inlineComments()) {
					writer.print(" " + inlineComment(comment));
				}
				writer.println();
			}
			else if (entry instanceof ContentSequence.Fragment<T> fragment) {
				writeFragment(writer, fragment.value());
			}
			else if (entry instanceof ContentSequence.Callback<T> callback) {
				callback.writer().accept(writer);
			}
		}
	}

}
