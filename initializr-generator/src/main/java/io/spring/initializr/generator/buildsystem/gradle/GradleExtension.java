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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import io.spring.initializr.generator.buildsystem.content.BuildFragment;
import io.spring.initializr.generator.buildsystem.content.BuildValue;
import io.spring.initializr.generator.buildsystem.content.ContentSequence;

/**
 * A customization for a Gradle extension.
 *
 * @author Moritz Halbritter
 * @author Sijun Yang
 */
public class GradleExtension {

	private final String name;

	private final List<Attribute> attributes;

	private final List<Invocation> invocations;

	private final Map<String, GradleExtension> nested;

	private final Set<String> importedTypes;

	private final ContentSequence<Object> content;

	private final boolean customContent;

	protected GradleExtension(Builder builder) {
		this.name = builder.name;
		this.attributes = List.copyOf(builder.attributes.values());
		this.invocations = List.copyOf(builder.invocations);
		this.nested = Collections.unmodifiableMap(resolve(builder.nested));
		this.importedTypes = collectImportedTypes(builder);
		this.customContent = builder.customContent;
		this.content = builder.content.build(Comparator.comparingInt(Builder::contentOrder))
			.map((entry) -> (entry instanceof Builder nestedBuilder)
					? Objects.requireNonNull(this.nested.get(nestedBuilder.name)) : entry);
	}

	private static Set<String> collectImportedTypes(Builder builder) {
		Set<String> result = new HashSet<>();
		addImportedTypes(result, builder);
		return Collections.unmodifiableSet(result);
	}

	private static void addImportedTypes(Set<String> importedTypes, Builder builder) {
		importedTypes.addAll(builder.importedTypes);
		for (Builder nested : builder.nested.values()) {
			addImportedTypes(importedTypes, nested);
		}
	}

	private static Map<String, GradleExtension> resolve(Map<String, Builder> extensions) {
		Map<String, GradleExtension> result = new LinkedHashMap<>();
		extensions.forEach((name, builder) -> result.put(name, builder.build()));
		return result;
	}

	/**
	 * Return the name of the extension.
	 * @return the extension name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return the attributes that should be configured for this extension.
	 * @return extension attributes
	 */
	public List<Attribute> getAttributes() {
		return this.attributes;
	}

	/**
	 * Return the {@link Invocation invocations} of this extension.
	 * @return extension invocations
	 */
	public List<Invocation> getInvocations() {
		return this.invocations;
	}

	/**
	 * Return nested {@link GradleExtension extensions}.
	 * @return nested extensions
	 */
	public Map<String, GradleExtension> getNested() {
		return this.nested;
	}

	/**
	 * Return the imported types.
	 * @return imported types
	 */
	public Set<String> getImportedTypes() {
		return this.importedTypes;
	}

	/**
	 * Return the complete body in output order.
	 * @return the content sequence
	 */
	public ContentSequence<Object> getContent() {
		return this.content;
	}

	boolean hasCustomContent() {
		return this.customContent;
	}

	/**
	 * Builder for {@link GradleExtension}.
	 */
	public static class Builder {

		private final String name;

		private final Map<String, Attribute> attributes = new LinkedHashMap<>();

		private final List<Invocation> invocations = new ArrayList<>();

		private final Map<String, Builder> nested = new LinkedHashMap<>();

		private final Set<String> importedTypes = new HashSet<>();

		private final ContentSequence.Builder<Object> content = new ContentSequence.Builder<>();

		private boolean customContent;

		protected Builder(String name) {
			this.name = name;
		}

		/**
		 * Import a given type.
		 * @param type the type to import
		 */
		public void importType(String type) {
			this.importedTypes.add(type);
		}

		/**
		 * Set a extension attribute.
		 * @param target the name of the attribute
		 * @param value the value
		 */
		public void attribute(String target, String value) {
			attribute(target, BuildValue.raw(value));
		}

		/**
		 * Set an extension attribute with an explicit text or raw value.
		 * @param target the name of the attribute
		 * @param value the value
		 */
		public void attribute(String target, BuildValue value) {
			Attribute attribute = Attribute.set(target, value);
			this.attributes.put(target, attribute);
			this.content.put("attribute:" + target, attribute);
		}

		/**
		 * Set an extension attribute to text quoted by the target writer.
		 * @param target the name of the attribute
		 * @param text the text
		 */
		public void attributeText(String target, String text) {
			attribute(target, BuildValue.text(text));
		}

		/**
		 * Set an extension attribute with a type.
		 * @param target the name of the attribute
		 * @param value the value
		 * @param type the type to import
		 */
		public void attributeWithType(String target, String value, String type) {
			importType(type);
			attribute(target, value);
		}

		/**
		 * Configure an extension attribute by appending the specified value.
		 * @param target the name of the attribute
		 * @param value the value to append
		 */
		public void append(String target, String value) {
			append(target, BuildValue.raw(value));
		}

		/**
		 * Append an explicit text or raw value to an extension attribute.
		 * @param target the name of the attribute
		 * @param value the value to append
		 */
		public void append(String target, BuildValue value) {
			Attribute attribute = Attribute.append(target, value);
			this.attributes.put(target, attribute);
			this.content.put("attribute:" + target, attribute);
		}

		/**
		 * Configure an extension attribute by appending the specified value and type.
		 * @param target the name of the attribute
		 * @param value the value to append
		 * @param type the type to import
		 */
		public void appendWithType(String target, String value, String type) {
			importType(type);
			append(target, value);
		}

		/**
		 * Invoke an extension method.
		 * @param target the name of the method
		 * @param arguments the arguments
		 */
		public void invoke(String target, String... arguments) {
			Invocation invocation = new Invocation(target, Arrays.asList(arguments));
			this.invocations.add(invocation);
			this.content.add("invocation:" + target, invocation);
		}

		/**
		 * Invoke an extension method with explicit text or raw arguments.
		 * @param target the name of the method
		 * @param arguments the arguments
		 */
		public void invokeValues(String target, BuildValue... arguments) {
			Invocation invocation = new Invocation(target, arguments);
			this.invocations.add(invocation);
			this.content.add("invocation:" + target, invocation);
		}

		/**
		 * Return the shared content extension point. Element keys are
		 * {@code attribute:name}, {@code invocation:name} and {@code nested:name}.
		 * Replacing an attribute or customizing a nested block retains its position.
		 * Calling this method opts into content-based rendering for this block. Without
		 * it, the writer continues to use the existing structured getters.
		 * @return the content builder
		 */
		public ContentSequence.Builder<Object> content() {
			this.customContent = true;
			return this.content;
		}

		private static int contentOrder(Object entry) {
			if (entry instanceof io.spring.initializr.generator.buildsystem.gradle.Invocation) {
				return 0;
			}
			return (entry instanceof io.spring.initializr.generator.buildsystem.gradle.Attribute) ? 1 : 2;
		}

		/**
		 * Insert a fragment at the current position, selecting insertion order.
		 * @param fragment the fragment
		 */
		public void fragment(BuildFragment fragment) {
			content().fragment(fragment);
		}

		/**
		 * Insert a comment at the current position, selecting insertion order.
		 * @param text the comment text, without delimiters
		 */
		public void comment(String text) {
			fragment(BuildFragment.comment(text));
		}

		/**
		 * Insert raw Gradle code at the current position, selecting insertion order.
		 * @param code code in the target DSL, without escaping
		 */
		public void raw(String code) {
			fragment(BuildFragment.raw(code));
		}

		/**
		 * Invoke an extension method.
		 * @param target the name of the method
		 * @param arguments the arguments
		 */
		public void invoke(String target, Collection<String> arguments) {
			Invocation invocation = new Invocation(target, List.copyOf(arguments));
			this.invocations.add(invocation);
			this.content.add("invocation:" + target, invocation);
		}

		/**
		 * Invoke an extension method.
		 * @param target the name of the method
		 * @param type the type to import
		 * @param arguments the arguments
		 */
		public void invokeWithType(String target, String type, String... arguments) {
			importType(type);
			invoke(target, arguments);
		}

		/**
		 * Invoke an extension method.
		 * @param target the name of the method
		 * @param type the type to import
		 * @param arguments the arguments
		 */
		public void invokeWithType(String target, String type, Collection<String> arguments) {
			importType(type);
			invoke(target, arguments);
		}

		/**
		 * Customize a nested extension for the specified name. If such nested extension
		 * has already been added, the consumer can be used to further tune the existing
		 * extension configuration.
		 * @param name a extension name
		 * @param customizer a {@link Consumer} to customize the nested extension
		 */
		public void nested(String name, Consumer<Builder> customizer) {
			Builder nestedBuilder = this.nested.computeIfAbsent(name, (ignored) -> new Builder(name));
			this.content.put("nested:" + name, nestedBuilder);
			customizer.accept(nestedBuilder);
		}

		/**
		 * Build a {@link GradleExtension} with the current state of this builder.
		 * @return a {@link GradleExtension}
		 */
		public GradleExtension build() {
			return new GradleExtension(this);
		}

	}

}
