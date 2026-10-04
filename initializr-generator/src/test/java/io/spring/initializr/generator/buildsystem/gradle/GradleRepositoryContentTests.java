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
import java.util.stream.Collectors;

import io.spring.initializr.generator.buildsystem.BuildContent;
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
	void credentialsCanBeContributedToBuildAndPluginRepositoriesInBothDialects() {
		GradleBuild build = new GradleBuild();
		MavenRepository repository = MavenRepository.withIdAndUrl("private", "${repoUrl}").build();
		build.repositories().add(repository);
		build.pluginRepositories().add(repository);
		addCredentials(build.repositoryContent("private"), "Build repository");
		addCredentials(build.pluginRepositoryContent("private"), "Plugin repository");
		for (GradleBuildWriter writer : new GradleBuildWriter[] { new GroovyDslGradleBuildWriter(),
				new KotlinDslGradleBuildWriter() }) {
			assertThat(write(writer, build)).contains("// Build repository").doesNotContain("Plugin repository");
			assertCredentials(write(writer, build));
		}
		for (GradleSettingsWriter writer : new GradleSettingsWriter[] { new GroovyDslGradleSettingsWriter(),
				new KotlinDslGradleSettingsWriter() }) {
			assertThat(write(writer, build)).contains("// Plugin repository").doesNotContain("Build repository");
			assertCredentials(write(writer, build));
		}
		assertThat(write(new GroovyDslGradleBuildWriter(), build)).contains("url = '${repoUrl}'");
		assertThat(write(new KotlinDslGradleBuildWriter(), build)).contains("url = uri(\"${repoUrl}\")");
		assertThat(write(new GroovyDslGradleSettingsWriter(), build)).contains("url = '${repoUrl}'");
		assertThat(write(new KotlinDslGradleSettingsWriter(), build)).contains("url = uri(\"${repoUrl}\")");
	}

	@Test
	void unknownRepositoryIdAndElementKeyAreRejected() {
		GradleBuild build = new GradleBuild();
		build.repositoryContent("missing").last().comment("reason");
		build.pluginRepositoryContent("missing").last().comment("reason");
		assertThatIllegalArgumentException().isThrownBy(() -> write(new GroovyDslGradleBuildWriter(), build))
			.withMessageContaining("missing");
		assertThatIllegalArgumentException().isThrownBy(() -> write(new KotlinDslGradleSettingsWriter(), build))
			.withMessageContaining("missing");
		build.repositories().add(MavenRepository.withIdAndUrl("missing", "https://example.com"));
		build.repositoryContent("missing").before("typo").raw("content");
		assertThatIllegalArgumentException().isThrownBy(() -> write(new GroovyDslGradleBuildWriter(), build))
			.withMessageContaining("typo");
	}

	private void addCredentials(BuildContent.Builder content, String reason) {
		content.before("url").comment(reason);
		content.after("url").write((writer) -> {
			writer.println("credentials {");
			writer.indented(() -> {
				writer.println("username = providers.gradleProperty(\"repoUsername\").get()");
				writer.println("password = providers.gradleProperty(\"repoPassword\").get()");
			});
			writer.println("}");
		});
	}

	private void assertCredentials(String written) {
		assertThat(written).contains("maven {\n");
		assertThat(written.lines().map(String::stripLeading).collect(Collectors.joining("\n"))).contains("""
				credentials {
					username = providers.gradleProperty("repoUsername").get()
					password = providers.gradleProperty("repoPassword").get()
				}
				""".lines().map(String::stripLeading).collect(Collectors.joining("\n")));
	}

	private String write(GradleBuildWriter writer, GradleBuild build) {
		StringWriter out = new StringWriter();
		writer.writeTo(new IndentingWriter(out, new SimpleIndentStrategy("\t")), build);
		return out.toString().replace("\r\n", "\n");
	}

	private String write(GradleSettingsWriter writer, GradleBuild build) {
		StringWriter out = new StringWriter();
		writer.writeTo(new IndentingWriter(out, new SimpleIndentStrategy("\t")), build);
		return out.toString().replace("\r\n", "\n");
	}

}
