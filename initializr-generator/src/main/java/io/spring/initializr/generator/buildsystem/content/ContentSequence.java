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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import io.spring.initializr.generator.io.IndentingWriter;
import org.jspecify.annotations.Nullable;

/**
 * An immutable sequence of structured elements, fragments and writer callbacks. Keys
 * identify structured elements within a block. Placement defaults to the first element
 * with the requested key; occurrence indexes can select repeated names. Movement selects
 * the first element with the requested key. No target syntax is parsed or translated.
 *
 * @param <T> the structured element type
 * @author Sijun Yang
 */
public final class ContentSequence<T> {

	private final List<Entry<T>> entries;

	private ContentSequence(List<Entry<T>> entries) {
		this.entries = List.copyOf(entries);
	}

	/**
	 * Return the entries in output order.
	 * @return the entries
	 */
	public List<Entry<T>> entries() {
		return this.entries;
	}

	/**
	 * Return the structured elements, excluding fragments and callbacks.
	 * @return the elements
	 */
	public List<T> elements() {
		return this.entries.stream()
			.filter(Element.class::isInstance)
			.map((entry) -> ((Element<T>) entry).value())
			.toList();
	}

	/**
	 * Resolve structured elements without changing their placement.
	 * @param resolver the element resolver
	 * @param <R> the resolved element type
	 * @return the resolved sequence
	 */
	public <R> ContentSequence<R> map(Function<T, R> resolver) {
		List<Entry<R>> result = new ArrayList<>();
		for (Entry<T> entry : this.entries) {
			if (entry instanceof Element<T> element) {
				result.add(new Element<>(element.key(), resolver.apply(element.value()), element.inlineComments()));
			}
			else if (entry instanceof Fragment<T> fragment) {
				result.add(new Fragment<>(fragment.value()));
			}
			else if (entry instanceof Callback<T> callback) {
				result.add(new Callback<>(callback.writer()));
			}
		}
		return new ContentSequence<>(result);
	}

	/**
	 * An entry in a content sequence.
	 *
	 * @param <T> the structured element type
	 */
	public sealed interface Entry<T> permits Element, Fragment, Callback {

	}

	/**
	 * A structured element and its trailing comments.
	 *
	 * @param key the element key
	 * @param value the element
	 * @param inlineComments the trailing comment texts
	 * @param <T> the structured element type
	 */
	public record Element<T>(String key, T value, List<String> inlineComments) implements Entry<T> {

		public Element(String key, T value, List<String> inlineComments) {
			this.key = Objects.requireNonNull(key, "key");
			this.value = Objects.requireNonNull(value, "value");
			this.inlineComments = List.copyOf(inlineComments);
		}

	}

	/**
	 * A standalone comment or raw fragment.
	 *
	 * @param value the fragment
	 * @param <T> the structured element type
	 */
	public record Fragment<T>(BuildFragment value) implements Entry<T> {

		public Fragment(BuildFragment value) {
			this.value = Objects.requireNonNull(value, "value");
		}

	}

	/**
	 * A writer callback that manages its own newlines and indentation.
	 *
	 * @param writer the callback
	 * @param <T> the structured element type
	 */
	public record Callback<T>(Consumer<IndentingWriter> writer) implements Entry<T> {

		public Callback(Consumer<IndentingWriter> writer) {
			this.writer = Objects.requireNonNull(writer, "writer");
		}

	}

	/**
	 * Builder shared by build model extension points. Adding unanchored fragments or
	 * callbacks selects insertion order. Anchored placement preserves the owning model's
	 * default order unless {@link #inInsertionOrder()} is selected explicitly.
	 *
	 * @param <T> the structured element type
	 */
	public static final class Builder<T> {

		private final List<Entry<T>> entries = new ArrayList<>();

		private final List<Insertion<T>> insertions = new ArrayList<>();

		private final Map<Anchor, List<String>> inlineComments = new LinkedHashMap<>();

		private final List<Move> moves = new ArrayList<>();

		private boolean insertionOrder;

		/**
		 * Register a structured element. Repeated keys remain separate elements.
		 * @param key the element key
		 * @param value the element
		 */
		public void add(String key, T value) {
			this.entries.add(new Element<>(key, value, List.of()));
		}

		/**
		 * Register or replace the first element with this key, retaining its position.
		 * @param key the element key
		 * @param value the element
		 */
		public void put(String key, T value) {
			int index = find(this.entries, key);
			Element<T> element = new Element<>(key, value, List.of());
			if (index < 0) {
				this.entries.add(element);
			}
			else {
				this.entries.set(index, element);
			}
		}

		/**
		 * Return registered structured elements in insertion order.
		 * @return the elements
		 */
		public List<T> elements() {
			return new ContentSequence<>(this.entries).elements();
		}

		/**
		 * Use insertion order instead of the owning model's default grouping.
		 * @return this builder
		 */
		public Builder<T> inInsertionOrder() {
			this.insertionOrder = true;
			return this;
		}

		/**
		 * Insert a fragment at the current position and select insertion order.
		 * @param fragment the fragment
		 * @return this builder
		 */
		public Builder<T> fragment(BuildFragment fragment) {
			this.entries.add(new Fragment<>(fragment));
			return inInsertionOrder();
		}

		/**
		 * Insert a comment at the current position.
		 * @param text the comment text without delimiters
		 * @return this builder
		 */
		public Builder<T> comment(String text) {
			return fragment(BuildFragment.comment(text));
		}

		/**
		 * Insert raw target syntax, preserving its internal whitespace.
		 * @param text the raw content
		 * @return this builder
		 */
		public Builder<T> raw(String text) {
			return fragment(BuildFragment.raw(text));
		}

		/**
		 * Insert a callback at the current position. It must write complete lines and
		 * balance its indentation using {@link IndentingWriter#indented(Runnable)}.
		 * @param writer the callback
		 * @return this builder
		 */
		public Builder<T> write(Consumer<IndentingWriter> writer) {
			this.entries.add(new Callback<>(writer));
			return inInsertionOrder();
		}

		/**
		 * Insert content at the beginning of the block.
		 * @return the placement
		 */
		public Placement<T> first() {
			return new Placement<>(this, Position.FIRST, null);
		}

		/**
		 * Insert content at the end of the block.
		 * @return the placement
		 */
		public Placement<T> last() {
			return new Placement<>(this, Position.LAST, null);
		}

		/**
		 * Insert content before the first element with this key.
		 * @param key the element key, resolved when building
		 * @return the placement
		 */
		public Placement<T> before(String key) {
			return before(key, 0);
		}

		/**
		 * Insert content after the first element with this key.
		 * @param key the element key, resolved when building
		 * @return the placement
		 */
		public Placement<T> after(String key) {
			return after(key, 0);
		}

		/**
		 * Insert content before a particular occurrence of an element key.
		 * @param key the element key
		 * @param occurrence the zero-based occurrence index
		 * @return the placement
		 */
		public Placement<T> before(String key, int occurrence) {
			return new Placement<>(this, Position.BEFORE, new Anchor(key, occurrence));
		}

		/**
		 * Insert content after a particular occurrence of an element key.
		 * @param key the element key
		 * @param occurrence the zero-based occurrence index
		 * @return the placement
		 */
		public Placement<T> after(String key, int occurrence) {
			return new Placement<>(this, Position.AFTER, new Anchor(key, occurrence));
		}

		/**
		 * Append a comment to the last line of the first element with this key.
		 * @param key the element key, resolved when building
		 * @param text single-line text without comment delimiters
		 * @return this builder
		 */
		public Builder<T> inlineComment(String key, String text) {
			return inlineComment(key, 0, text);
		}

		/**
		 * Append a comment to a particular occurrence of an element key.
		 * @param key the element key
		 * @param occurrence the zero-based occurrence index
		 * @param text single-line text without delimiters
		 * @return this builder
		 */
		public Builder<T> inlineComment(String key, int occurrence, String text) {
			Anchor anchor = new Anchor(key, occurrence);
			Objects.requireNonNull(text, "text");
			if (text.contains("\n") || text.contains("\r")) {
				throw new IllegalArgumentException("Inline comments must contain a single line");
			}
			this.inlineComments.computeIfAbsent(anchor, (ignored) -> new ArrayList<>()).add(text);
			return this;
		}

		/**
		 * Move the first element with this key before another element.
		 * @param key the element to move
		 * @param anchor the destination element
		 * @return this builder
		 */
		public Builder<T> moveBefore(String key, String anchor) {
			this.moves.add(new Move(key, anchor, false));
			return this;
		}

		/**
		 * Move the first element with this key after another element.
		 * @param key the element to move
		 * @param anchor the destination element
		 * @return this builder
		 */
		public Builder<T> moveAfter(String key, String anchor) {
			this.moves.add(new Move(key, anchor, true));
			return this;
		}

		/**
		 * Build a snapshot in insertion order.
		 * @return the sequence
		 */
		public ContentSequence<T> build() {
			return build(null);
		}

		/**
		 * Build a snapshot, using the supplied default order unless insertion order has
		 * been selected. Missing placement or movement keys are rejected.
		 * @param defaultOrder the default element order, or {@code null}
		 * @return the sequence
		 */
		public ContentSequence<T> build(@Nullable Comparator<? super T> defaultOrder) {
			List<Entry<T>> ordered = new ArrayList<>(this.entries);
			if (!this.insertionOrder && defaultOrder != null) {
				ordered.sort((left, right) -> defaultOrder.compare(((Element<T>) left).value(),
						((Element<T>) right).value()));
			}
			for (Move move : this.moves) {
				int index = require(ordered, move.key());
				require(ordered, move.anchor());
				if (!move.key().equals(move.anchor())) {
					Entry<T> entry = ordered.remove(index);
					ordered.add(require(ordered, move.anchor()) + (move.after() ? 1 : 0), entry);
				}
			}
			this.inlineComments.keySet().forEach((anchor) -> require(ordered, anchor));
			this.insertions.stream()
				.filter((insertion) -> insertion.anchor() != null)
				.forEach((insertion) -> require(ordered, insertion.anchor()));
			List<Entry<T>> result = new ArrayList<>();
			appendInsertions(result, Position.FIRST, null);
			Map<String, Integer> occurrences = new LinkedHashMap<>();
			for (Entry<T> entry : ordered) {
				if (entry instanceof Element<T> element) {
					int occurrence = occurrences.merge(element.key(), 1, Integer::sum) - 1;
					Anchor anchor = new Anchor(element.key(), occurrence);
					appendInsertions(result, Position.BEFORE, anchor);
					result.add(new Element<>(element.key(), element.value(),
							this.inlineComments.getOrDefault(anchor, List.of())));
					appendInsertions(result, Position.AFTER, anchor);
				}
				else {
					result.add(entry);
				}
			}
			appendInsertions(result, Position.LAST, null);
			return new ContentSequence<>(result);
		}

		private void appendInsertions(List<Entry<T>> result, Position position, @Nullable Anchor anchor) {
			this.insertions.stream()
				.filter((insertion) -> insertion.position() == position && Objects.equals(insertion.anchor(), anchor))
				.forEach((insertion) -> result.add(insertion.entry()));
		}

		private int require(List<Entry<T>> candidates, String key) {
			return require(candidates, new Anchor(key, 0));
		}

		private int require(List<Entry<T>> candidates, @Nullable Anchor anchor) {
			Objects.requireNonNull(anchor, "anchor");
			int occurrence = 0;
			for (int i = 0; i < candidates.size(); i++) {
				if (candidates.get(i) instanceof Element<T> element && element.key().equals(anchor.key())) {
					if (occurrence++ == anchor.occurrence()) {
						return i;
					}
				}
			}
			throw new IllegalArgumentException("No structured content element with key '" + anchor.key()
					+ "' at occurrence " + anchor.occurrence());
		}

		private int find(List<Entry<T>> candidates, @Nullable String key) {
			for (int i = 0; i < candidates.size(); i++) {
				if (candidates.get(i) instanceof Element<T> element && element.key().equals(key)) {
					return i;
				}
			}
			return -1;
		}

	}

	/**
	 * Content attached to a block boundary or a structured element.
	 *
	 * @param <T> the structured element type
	 */
	public static final class Placement<T> {

		private final Builder<T> builder;

		private final Position position;

		private final @Nullable Anchor anchor;

		private Placement(Builder<T> builder, Position position, @Nullable Anchor anchor) {
			this.builder = builder;
			this.position = position;
			this.anchor = anchor;
		}

		/**
		 * Insert a fragment at this placement.
		 * @param fragment the fragment
		 * @return this placement
		 */
		public Placement<T> fragment(BuildFragment fragment) {
			this.builder.insertions.add(new Insertion<>(this.position, this.anchor, new Fragment<>(fragment)));
			return this;
		}

		/**
		 * Insert a comment at this placement.
		 * @param text the comment text without delimiters
		 * @return this placement
		 */
		public Placement<T> comment(String text) {
			return fragment(BuildFragment.comment(text));
		}

		/**
		 * Insert raw target syntax at this placement.
		 * @param text the raw content
		 * @return this placement
		 */
		public Placement<T> raw(String text) {
			return fragment(BuildFragment.raw(text));
		}

		/**
		 * Insert a callback that writes complete lines at this placement.
		 * @param writer the callback
		 * @return this placement
		 */
		public Placement<T> write(Consumer<IndentingWriter> writer) {
			this.builder.insertions.add(new Insertion<>(this.position, this.anchor, new Callback<>(writer)));
			return this;
		}

	}

	private enum Position {

		FIRST, BEFORE, AFTER, LAST

	}

	private record Insertion<T>(Position position, @Nullable Anchor anchor, Entry<T> entry) {
	}

	private record Anchor(String key, int occurrence) {

		private Anchor {
			Objects.requireNonNull(key, "key");
			if (occurrence < 0) {
				throw new IllegalArgumentException("Occurrence must be non-negative");
			}
		}

	}

	private record Move(String key, String anchor, boolean after) {

		private Move {
			Objects.requireNonNull(key, "key");
			Objects.requireNonNull(anchor, "anchor");
		}

	}

}
