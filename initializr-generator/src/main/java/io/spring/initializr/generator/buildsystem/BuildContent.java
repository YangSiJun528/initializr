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

package io.spring.initializr.generator.buildsystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.spring.initializr.generator.io.IndentingWriter;
import org.jspecify.annotations.Nullable;

/**
 * Additional content around existing elements of a build block. Structured elements keep
 * their original order and representation. Raw content and callbacks use the target
 * syntax; only comments are rendered by the owning build writer.
 *
 * @author Sijun Yang
 */
public final class BuildContent {

	/** An empty contribution set. */
	public static final BuildContent EMPTY = new BuildContent(List.of());

	private final List<Insertion> insertions;

	private BuildContent(List<Insertion> insertions) {
		this.insertions = List.copyOf(insertions);
	}

	/**
	 * Return whether any content has been registered.
	 * @return whether this content is empty
	 */
	public boolean isEmpty() {
		return this.insertions.isEmpty();
	}

	/**
	 * Write existing elements with the registered contributions. Named placements apply
	 * to the first matching element. Missing keys are rejected before writing this block.
	 * @param writer the output
	 * @param elements the existing elements, in their original order
	 * @param key the element key resolver
	 * @param elementWriter the existing element renderer
	 * @param commentWriter the target syntax's comment renderer
	 * @param <T> the element type
	 */
	public <T> void writeTo(IndentingWriter writer, Collection<T> elements, Function<T, String> key,
			BiConsumer<IndentingWriter, T> elementWriter, BiConsumer<IndentingWriter, String> commentWriter) {
		if (isEmpty()) {
			elements.forEach((element) -> elementWriter.accept(writer, element));
			return;
		}
		Set<String> keys = elements.stream().map(key).collect(Collectors.toSet());
		for (Insertion insertion : this.insertions) {
			if (insertion.key() != null && !keys.contains(insertion.key())) {
				throw new IllegalArgumentException("No content element with key '" + insertion.key() + "'");
			}
		}
		writeInsertions(writer, Position.FIRST, null, commentWriter);
		Set<String> seen = new HashSet<>();
		for (T element : elements) {
			String elementKey = key.apply(element);
			boolean first = seen.add(elementKey);
			if (first) {
				writeInsertions(writer, Position.BEFORE, elementKey, commentWriter);
			}
			elementWriter.accept(writer, element);
			if (first) {
				writeInsertions(writer, Position.AFTER, elementKey, commentWriter);
			}
		}
		writeInsertions(writer, Position.LAST, null, commentWriter);
	}

	private void writeInsertions(IndentingWriter writer, Position position, @Nullable String key,
			BiConsumer<IndentingWriter, String> commentWriter) {
		this.insertions.stream()
			.filter((insertion) -> insertion.position() == position && Objects.equals(insertion.key(), key))
			.forEach((insertion) -> {
				if (insertion.comment() != null) {
					commentWriter.accept(writer, insertion.comment());
				}
				else {
					Objects.requireNonNull(insertion.callback()).accept(writer);
				}
			});
	}

	/** Builder for content contributed to an existing block. */
	public static final class Builder {

		private final List<Insertion> insertions = new ArrayList<>();

		/**
		 * Insert at the beginning of the block.
		 * @return the placement
		 */
		public Placement first() {
			return new Placement(this, Position.FIRST, null);
		}

		/**
		 * Insert at the end of the block.
		 * @return the placement
		 */
		public Placement last() {
			return new Placement(this, Position.LAST, null);
		}

		/**
		 * Insert before the first element with this key.
		 * @param key the element key
		 * @return the placement
		 */
		public Placement before(String key) {
			return new Placement(this, Position.BEFORE, Objects.requireNonNull(key, "key"));
		}

		/**
		 * Insert after the first element with this key.
		 * @param key the element key
		 * @return the placement
		 */
		public Placement after(String key) {
			return new Placement(this, Position.AFTER, Objects.requireNonNull(key, "key"));
		}

		/**
		 * Build an immutable snapshot.
		 * @return the content
		 */
		public BuildContent build() {
			return new BuildContent(this.insertions);
		}

	}

	/** A placement for comments, raw syntax or writer callbacks. */
	public static final class Placement {

		private final Builder builder;

		private final Position position;

		private final @Nullable String key;

		private Placement(Builder builder, Position position, @Nullable String key) {
			this.builder = builder;
			this.position = position;
			this.key = key;
		}

		/**
		 * Insert a comment rendered using the target syntax.
		 * @param text text without comment delimiters
		 * @return this placement
		 */
		public Placement comment(String text) {
			this.builder.insertions
				.add(new Insertion(this.position, this.key, Objects.requireNonNull(text, "text"), null));
			return this;
		}

		/**
		 * Insert raw target syntax, preserving internal whitespace and adding a newline.
		 * @param text the raw text
		 * @return this placement
		 */
		public Placement raw(String text) {
			Objects.requireNonNull(text, "text");
			return write((writer) -> writer.println(text));
		}

		/**
		 * Insert a callback that writes complete lines and balances its indentation.
		 * @param callback the writer callback
		 * @return this placement
		 */
		public Placement write(Consumer<IndentingWriter> callback) {
			this.builder.insertions
				.add(new Insertion(this.position, this.key, null, Objects.requireNonNull(callback, "callback")));
			return this;
		}

	}

	private enum Position {

		FIRST, BEFORE, AFTER, LAST

	}

	private record Insertion(Position position, @Nullable String key, @Nullable String comment,
			@Nullable Consumer<IndentingWriter> callback) {
	}

}
