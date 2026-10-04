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
import java.util.function.Consumer;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.BuildContent;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.MavenRepositoryContainer;
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

	private final Map<String, BuildContent.Builder> repositoryContent = new LinkedHashMap<>();

	private final Map<String, BuildContent.Builder> pluginRepositoryContent = new LinkedHashMap<>();

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
	 * Contribute inside a registered repository's {@code maven} block. The {@code url}
	 * key identifies the generated URL assignment. Raw code uses the selected DSL.
	 * @param id the repository ID
	 * @return the content builder
	 */
	public BuildContent.Builder repositoryContent(String id) {
		return this.repositoryContent.computeIfAbsent(id, (key) -> new BuildContent.Builder());
	}

	/**
	 * Contribute inside a registered plugin repository's {@code maven} block in the
	 * settings file. The {@code url} key identifies the generated URL assignment.
	 * @param id the repository ID
	 * @return the content builder
	 */
	public BuildContent.Builder pluginRepositoryContent(String id) {
		return this.pluginRepositoryContent.computeIfAbsent(id, (key) -> new BuildContent.Builder());
	}

	BuildContent getRepositoryContent(String id) {
		return snapshot(this.repositoryContent, id);
	}

	BuildContent getPluginRepositoryContent(String id) {
		return snapshot(this.pluginRepositoryContent, id);
	}

	private BuildContent snapshot(Map<String, BuildContent.Builder> content, String id) {
		BuildContent.Builder builder = content.get(id);
		return (builder != null) ? builder.build() : BuildContent.EMPTY;
	}

	void validateRepositoryContent() {
		validateContent(this.repositoryContent, repositories());
	}

	void validatePluginRepositoryContent() {
		validateContent(this.pluginRepositoryContent, pluginRepositories());
	}

	private void validateContent(Map<String, BuildContent.Builder> content, MavenRepositoryContainer repositories) {
		for (String id : content.keySet()) {
			if (repositories.items().noneMatch((repository) -> repository.getId().equals(id))) {
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
