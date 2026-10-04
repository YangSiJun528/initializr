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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.buildsystem.MavenRepositoryContainer;
import io.spring.initializr.generator.buildsystem.content.ContentSequence;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSettings.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Gradle-specific {@linkplain Build build configuration}.
 *
 * @author Andy Wilkinson
 * @author Jean-Baptiste Nizet
 * @author Moritz Halbritter
 * @author Sijun Yang
 */
public class GradleBuild extends Build {

	private final GradleBuildSettings.Builder settings = new Builder();

	private final GradlePluginContainer plugins = new GradlePluginContainer();

	private final GradleConfigurationContainer configurations = new GradleConfigurationContainer();

	private final GradleTaskContainer tasks = new GradleTaskContainer();

	private final GradleSnippetContainer snippets = new GradleSnippetContainer();

	private final GradleBuildscript.Builder buildscript = new GradleBuildscript.Builder();

	private final GradleExtensionContainer extensions = new GradleExtensionContainer();

	private final Map<String, ContentSequence.Builder<String>> repositoryContent = new LinkedHashMap<>();

	private final Map<String, ContentSequence.Builder<String>> pluginRepositoryContent = new LinkedHashMap<>();

	/**
	 * Create a new Gradle build using the specified {@link BuildItemResolver}.
	 * @param buildItemResolver the build item resolved to use
	 */
	public GradleBuild(@Nullable BuildItemResolver buildItemResolver) {
		super(buildItemResolver);
	}

	/**
	 * Create a new Gradle build without a build item resolver.
	 */
	public GradleBuild() {
		this(null);
	}

	@Override
	public GradleBuildSettings.Builder settings() {
		return this.settings;
	}

	@Override
	public GradleBuildSettings getSettings() {
		return this.settings.build();
	}

	/**
	 * Return the {@linkplain GradlePluginContainer plugin container} to use to configure
	 * plugins.
	 * @return the {@link GradlePluginContainer}
	 */
	public GradlePluginContainer plugins() {
		return this.plugins;
	}

	/**
	 * Return the {@linkplain GradleConfigurationContainer configuration container} to use
	 * for configuration customizations.
	 * @return the {@link GradleConfigurationContainer}
	 */
	public GradleConfigurationContainer configurations() {
		return this.configurations;
	}

	/**
	 * Return the {@linkplain GradleTaskContainer task container} to use to configure
	 * tasks.
	 * @return the {@link GradleTaskContainer}
	 */
	public GradleTaskContainer tasks() {
		return this.tasks;
	}

	/**
	 * Return the {@linkplain GradleExtensionContainer extension container} to use to
	 * configure extensions.
	 * @return the {@link GradleExtensionContainer}
	 */
	public GradleExtensionContainer extensions() {
		return this.extensions;
	}

	/**
	 * Return the {@linkplain GradleSnippetContainer snippet container} to use to apply
	 * snippets.
	 * @return the {@link GradleSnippetContainer}
	 */
	public GradleSnippetContainer snippets() {
		return this.snippets;
	}

	/**
	 * Return the shared extension point inside a repository's {@code maven} block. The
	 * {@code url} key identifies the generated URL assignment. The repository must be
	 * registered in {@link #repositories()} before writing the build. Raw content and
	 * callbacks must use the selected Gradle DSL.
	 * @param id the repository ID
	 * @return the content builder
	 */
	public ContentSequence.Builder<String> repositoryContent(String id) {
		return repositoryContent(this.repositoryContent, id);
	}

	/**
	 * Return the shared extension point inside a plugin repository's {@code maven} block
	 * in the settings file. The {@code url} key identifies the URL assignment. The
	 * repository must be registered in {@link #pluginRepositories()} before writing the
	 * settings file.
	 * @param id the repository ID
	 * @return the content builder
	 */
	public ContentSequence.Builder<String> pluginRepositoryContent(String id) {
		return repositoryContent(this.pluginRepositoryContent, id);
	}

	private ContentSequence.Builder<String> repositoryContent(Map<String, ContentSequence.Builder<String>> content,
			String id) {
		Objects.requireNonNull(id, "id");
		return content.computeIfAbsent(id, (ignored) -> {
			ContentSequence.Builder<String> builder = new ContentSequence.Builder<>();
			builder.add("url", "url");
			return builder;
		});
	}

	@Nullable ContentSequence<String> getRepositoryContent(String id) {
		return repositoryContentSnapshot(this.repositoryContent, id);
	}

	@Nullable ContentSequence<String> getPluginRepositoryContent(String id) {
		return repositoryContentSnapshot(this.pluginRepositoryContent, id);
	}

	private @Nullable ContentSequence<String> repositoryContentSnapshot(
			Map<String, ContentSequence.Builder<String>> content, String id) {
		ContentSequence.Builder<String> builder = content.get(id);
		return (builder != null) ? builder.build() : null;
	}

	void validateRepositoryContent() {
		validateRepositoryContent(this.repositoryContent, repositories());
	}

	void validatePluginRepositoryContent() {
		validateRepositoryContent(this.pluginRepositoryContent, pluginRepositories());
	}

	private void validateRepositoryContent(Map<String, ContentSequence.Builder<String>> content,
			MavenRepositoryContainer repositories) {
		Set<String> ids = repositories.items().map(MavenRepository::getId).collect(Collectors.toSet());
		for (String id : content.keySet()) {
			if (!ids.contains(id)) {
				throw new IllegalArgumentException("No registered repository with ID '" + id + "'");
			}
		}
	}

	/**
	 * Customize the {@code buildscript} of the build using the specified consumer.
	 * @param buildscript a consumer of the current buildscript
	 */
	public void buildscript(Consumer<GradleBuildscript.Builder> buildscript) {
		buildscript.accept(this.buildscript);
	}

	/**
	 * Return the {@link GradleBuildscript buildscript} of this build.
	 * @return the buildscript to use
	 */
	public GradleBuildscript getBuildscript() {
		return this.buildscript.build();
	}

}
