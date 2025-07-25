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

package io.spring.initializr.generator.spring.build.gradle;

import java.util.List;
import java.util.stream.Collectors;

import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.gradle.GroovyDslGradleBuildWriter;
import io.spring.initializr.generator.buildsystem.gradle.GroovyDslGradleSettingsWriter;
import io.spring.initializr.generator.buildsystem.gradle.KotlinDslGradleBuildWriter;
import io.spring.initializr.generator.buildsystem.gradle.KotlinDslGradleSettingsWriter;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnLanguage;
import io.spring.initializr.generator.condition.ConditionalOnPackaging;
import io.spring.initializr.generator.condition.ConditionalOnPlatformVersion;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.language.groovy.GroovyLanguage;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import io.spring.initializr.generator.spring.util.LambdaSafe;
import io.spring.initializr.metadata.InitializrMetadata;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration for contributions specific to the generation of a project that will use
 * Gradle as its build system.
 *
 * @author Andy Wilkinson
 * @author Jean-Baptiste Nizet
 */
@ProjectGenerationConfiguration
@ConditionalOnBuildSystem(GradleBuildSystem.ID)
public class GradleProjectGenerationConfiguration {

	private static final int LANGUAGE_PLUGINS_ORDER = Ordered.HIGHEST_PRECEDENCE + 5;

	private static final int PACKAGING_PLUGINS_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

	private static final int TEST_ORDER = 100;

	private final IndentingWriterFactory indentingWriterFactory;

	public GradleProjectGenerationConfiguration(IndentingWriterFactory indentingWriterFactory) {
		this.indentingWriterFactory = indentingWriterFactory;
	}

	@Bean
	public GradleBuild gradleBuild(ObjectProvider<BuildItemResolver> buildItemResolver,
			ObjectProvider<BuildCustomizer<?>> buildCustomizers) {
		return createGradleBuild(buildItemResolver.getIfAvailable(),
				buildCustomizers.orderedStream().collect(Collectors.toList()));
	}

	@SuppressWarnings("unchecked")
	private GradleBuild createGradleBuild(BuildItemResolver buildItemResolver,
			List<BuildCustomizer<?>> buildCustomizers) {
		GradleBuild build = (buildItemResolver != null) ? new GradleBuild(buildItemResolver) : new GradleBuild();
		LambdaSafe.callbacks(BuildCustomizer.class, buildCustomizers, build)
			.invoke((customizer) -> customizer.customize(build));
		return build;
	}

	@Bean
	public BuildCustomizer<GradleBuild> defaultGradleBuildCustomizer(ProjectDescription description) {
		return (build) -> build.settings().sourceCompatibility(description.getLanguage().jvmVersion());
	}

	@Bean
	public GradleConfigurationBuildCustomizer gradleConfigurationBuildCustomizer() {
		return new GradleConfigurationBuildCustomizer();
	}

	@Bean
	@ConditionalOnLanguage(JavaLanguage.ID)
	public BuildCustomizer<GradleBuild> javaPluginContributor() {
		return BuildCustomizer.ordered(LANGUAGE_PLUGINS_ORDER, (build) -> build.plugins().add("java"));
	}

	@Bean
	@ConditionalOnLanguage(GroovyLanguage.ID)
	public BuildCustomizer<GradleBuild> groovyPluginContributor() {
		return BuildCustomizer.ordered(LANGUAGE_PLUGINS_ORDER, (build) -> build.plugins().add("groovy"));
	}

	@Bean
	@ConditionalOnPackaging(WarPackaging.ID)
	public BuildCustomizer<GradleBuild> warPluginContributor() {
		return BuildCustomizer.ordered(PACKAGING_PLUGINS_ORDER, (build) -> build.plugins().add("war"));
	}

	@Bean
	@ConditionalOnBuildSystem(id = GradleBuildSystem.ID)
	BuildCustomizer<GradleBuild> springBootPluginContributor(ProjectDescription description,
			ObjectProvider<DependencyManagementPluginVersionResolver> versionResolver, InitializrMetadata metadata) {
		return new SpringBootPluginBuildCustomizer(description, versionResolver
			.getIfAvailable(() -> new InitializrDependencyManagementPluginVersionResolver(metadata)));
	}

	@Bean
	@ConditionalOnBuildSystem(id = GradleBuildSystem.ID, dialect = GradleBuildSystem.DIALECT_GROOVY)
	public GradleBuildProjectContributor gradleBuildProjectContributor(GroovyDslGradleBuildWriter buildWriter,
			GradleBuild build) {
		return new GradleBuildProjectContributor(buildWriter, build, this.indentingWriterFactory, "build.gradle");
	}

	@Bean
	@ConditionalOnBuildSystem(id = GradleBuildSystem.ID, dialect = GradleBuildSystem.DIALECT_KOTLIN)
	public GradleBuildProjectContributor gradleKtsBuildProjectContributor(KotlinDslGradleBuildWriter buildWriter,
			GradleBuild build) {
		return new GradleBuildProjectContributor(buildWriter, build, this.indentingWriterFactory, "build.gradle.kts");
	}

	/**
	 * Configuration specific to projects using Gradle 8.
	 */
	@Configuration
	@ConditionalOnGradleVersion("8")
	static class Gradle8ProjectGenerationConfiguration {

		@Bean
		GradleWrapperContributor gradle8WrapperContributor() {
			return new GradleWrapperContributor("8");
		}

	}

	/**
	 * Configuration specific to projects using Gradle (Groovy DSL).
	 */
	@Configuration
	@ConditionalOnBuildSystem(id = GradleBuildSystem.ID, dialect = GradleBuildSystem.DIALECT_GROOVY)
	static class GradleGroovyProjectGenerationConfiguration {

		@Bean
		GroovyDslGradleBuildWriter gradleBuildWriter() {
			return new GroovyDslGradleBuildWriter();
		}

		@Bean
		SettingsGradleProjectContributor settingsGradleProjectContributor(GradleBuild build,
				IndentingWriterFactory indentingWriterFactory) {
			return new SettingsGradleProjectContributor(build, indentingWriterFactory,
					new GroovyDslGradleSettingsWriter(), "settings.gradle");
		}

		@Bean
		@ConditionalOnPlatformVersion("2.2.0.M3")
		BuildCustomizer<GradleBuild> testTaskContributor() {
			return BuildCustomizer.ordered(TEST_ORDER,
					(build) -> build.tasks().customize("test", (test) -> test.invoke("useJUnitPlatform")));
		}

		@Bean
		GradleAnnotationProcessorScopeBuildCustomizer gradleAnnotationProcessorScopeBuildCustomizer() {
			return new GradleAnnotationProcessorScopeBuildCustomizer();
		}

	}

	/**
	 * Configuration specific to projects using Gradle (Kotlin DSL).
	 */
	@Configuration
	@ConditionalOnBuildSystem(id = GradleBuildSystem.ID, dialect = GradleBuildSystem.DIALECT_KOTLIN)
	static class GradleKtsProjectGenerationConfiguration {

		@Bean
		KotlinDslGradleBuildWriter gradleKtsBuildWriter() {
			return new KotlinDslGradleBuildWriter();
		}

		@Bean
		SettingsGradleProjectContributor settingsGradleKtsProjectContributor(GradleBuild build,
				IndentingWriterFactory indentingWriterFactory) {
			return new SettingsGradleProjectContributor(build, indentingWriterFactory,
					new KotlinDslGradleSettingsWriter(), "settings.gradle.kts");
		}

		@Bean
		@ConditionalOnPlatformVersion("2.2.0.M3")
		BuildCustomizer<GradleBuild> testTaskContributor() {
			return BuildCustomizer.ordered(TEST_ORDER,
					(build) -> build.tasks().customizeWithType("Test", (test) -> test.invoke("useJUnitPlatform")));
		}

		@Bean
		GradleAnnotationProcessorScopeBuildCustomizer gradleAnnotationProcessorScopeBuildCustomizer() {
			return new GradleAnnotationProcessorScopeBuildCustomizer();
		}

	}

}
