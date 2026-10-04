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

import java.io.StringWriter;

import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.generator.io.SimpleIndentStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for shared content in plugin repository blocks in both Gradle DSLs.
 *
 * @author Sijun Yang
 */
class GradleRepositoryContentTests {

	@Test
	void repositoriesWithoutContentPreserveOriginalUrlInterpolation() {
		GradleBuild build = new GradleBuild();
		MavenRepository repository = MavenRepository.withIdAndUrl("private", "${repoUrl}").build();
		build.repositories().add(repository);
		build.pluginRepositories().add(repository);
		StringWriter groovy = new StringWriter();
		new GroovyDslGradleBuildWriter().writeTo(new IndentingWriter(groovy), build);
		StringWriter kotlin = new StringWriter();
		new KotlinDslGradleBuildWriter().writeTo(new IndentingWriter(kotlin), build);
		assertThat(groovy.toString()).contains("maven { url = '${repoUrl}' }");
		assertThat(kotlin.toString()).contains("maven { url = uri(\"${repoUrl}\") }");
		assertThat(write(new GroovyDslGradleSettingsWriter(), build)).contains("maven { url = '${repoUrl}' }");
		assertThat(write(new KotlinDslGradleSettingsWriter(), build)).contains("maven { url = uri(\"${repoUrl}\") }");
	}

	@Test
	void legacyGroovySettingsQuoteOverrideStillAppliesToRepositoryUrls() {
		GradleBuild build = new GradleBuild();
		build.pluginRepositories().add(MavenRepository.withIdAndUrl("private", "https://plugins.example.com"));
		GradleSettingsWriter writer = new GroovyDslGradleSettingsWriter() {
			@Override
			protected String wrapWithQuotes(String value) {
				return "\"" + value + "\"";
			}
		};
		assertThat(write(writer, build)).contains("maven { url = \"https://plugins.example.com\" }");
	}

	@Test
	void extendedRepositoryUrlsAreQuotedForTheTargetDsl() {
		GradleBuild build = new GradleBuild();
		MavenRepository repository = MavenRepository.withIdAndUrl("private", "https://example.com/a'b/$repo").build();
		build.repositories().add(repository);
		build.pluginRepositories().add(repository);
		build.repositoryContent("private").inlineComment("url", "URL");
		build.pluginRepositoryContent("private").inlineComment("url", "URL");
		StringWriter groovy = new StringWriter();
		new GroovyDslGradleBuildWriter().writeTo(new IndentingWriter(groovy), build);
		StringWriter kotlin = new StringWriter();
		new KotlinDslGradleBuildWriter().writeTo(new IndentingWriter(kotlin), build);
		String groovyAssignment = "url = 'https://example.com/a\\'b/$repo' // URL";
		String kotlinAssignment = "url = uri(\"https://example.com/a'b/\\$repo\") // URL";
		assertThat(groovy.toString()).contains(groovyAssignment);
		assertThat(kotlin.toString()).contains(kotlinAssignment);
		assertThat(write(new GroovyDslGradleSettingsWriter(), build)).contains(groovyAssignment);
		assertThat(write(new KotlinDslGradleSettingsWriter(), build)).contains(kotlinAssignment);
	}

	@Test
	void pluginRepositoryContentUsesTheSamePlacementAndCallbackContract() {
		GradleBuild build = new GradleBuild();
		build.pluginRepositories().add(MavenRepository.withIdAndUrl("private", "https://plugins.example.com"));
		build.pluginRepositoryContent("private").before("url").comment("Private plugins");
		build.pluginRepositoryContent("private").inlineComment("url", "URL reason");
		build.pluginRepositoryContent("private").after("url").write((writer) -> {
			writer.println("credentials {");
			writer.indented(() -> writer.println("username = providers.gradleProperty(\"repoUsername\").get()"));
			writer.println("}");
		});
		for (GradleSettingsWriter settingsWriter : settingsWriters()) {
			assertThat(write(settingsWriter, build)).contains("""
					pluginManagement {
						repositories {
							maven {
								// Private plugins
					""").contains(" // URL reason\n").contains("""
								credentials {
									username = providers.gradleProperty("repoUsername").get()
								}
							}
							gradlePluginPortal()
						}
					}
					""");
		}
		assertThat(write(new GroovyDslGradleSettingsWriter(), build))
			.contains("url = 'https://plugins.example.com' // URL reason");
		assertThat(write(new KotlinDslGradleSettingsWriter(), build))
			.contains("url = uri(\"https://plugins.example.com\") // URL reason");
	}

	@Test
	void pluginRepositoryContentIsKeptSeparateFromBuildRepositoryContent() {
		GradleBuild build = new GradleBuild();
		build.repositories().add(MavenRepository.withIdAndUrl("private", "https://artifacts.example.com"));
		build.pluginRepositories().add(MavenRepository.withIdAndUrl("private", "https://plugins.example.com"));
		build.repositoryContent("private").comment("build only");
		build.pluginRepositoryContent("private").comment("settings only");
		for (GradleSettingsWriter settingsWriter : settingsWriters()) {
			assertThat(write(settingsWriter, build)).contains("// settings only").doesNotContain("build only");
		}
		StringWriter out = new StringWriter();
		new GroovyDslGradleBuildWriter().writeTo(new IndentingWriter(out), build);
		assertThat(out.toString()).contains("// build only").doesNotContain("settings only");
	}

	@Test
	void unknownPluginRepositoryAndUrlAnchorAreRejected() {
		GradleBuild build = new GradleBuild();
		build.pluginRepositoryContent("missing").comment("reason");
		for (GradleSettingsWriter settingsWriter : settingsWriters()) {
			assertThatIllegalArgumentException().isThrownBy(() -> write(settingsWriter, build))
				.withMessageContaining("missing");
		}
		GradleBuild unknownAnchor = new GradleBuild();
		unknownAnchor.pluginRepositories().add("maven-central");
		unknownAnchor.pluginRepositoryContent("maven-central").before("missing").comment("reason");
		for (GradleSettingsWriter settingsWriter : settingsWriters()) {
			assertThatIllegalArgumentException().isThrownBy(() -> write(settingsWriter, unknownAnchor))
				.withMessageContaining("missing");
		}
	}

	private GradleSettingsWriter[] settingsWriters() {
		return new GradleSettingsWriter[] { new GroovyDslGradleSettingsWriter(), new KotlinDslGradleSettingsWriter() };
	}

	private String write(GradleSettingsWriter writer, GradleBuild build) {
		StringWriter out = new StringWriter();
		writer.writeTo(new IndentingWriter(out, new SimpleIndentStrategy("\t")), build);
		return out.toString().replace("\r\n", "\n");
	}

}
