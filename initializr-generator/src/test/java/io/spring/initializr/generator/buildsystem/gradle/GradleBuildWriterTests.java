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

import java.util.Set;

import io.spring.initializr.generator.buildsystem.MavenRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Common tests for {@link GradleBuildWriter} implementations.
 *
 * @author Stephane Nicoll
 * @author Sijun Yang
 */
public abstract class GradleBuildWriterTests {

	@Test
	void contentIsInterleavedWithStructuredElementsAndNestedClosingComments() {
		GradleBuild build = new GradleBuild();
		build.extensions().customize("custom", (extension) -> {
			extension.attribute("first", "1");
			extension.content().comment("between");
			extension.invoke("reset");
			extension.content().raw("middle = 2");
			extension.nested("options", (nested) -> nested.attribute("enabled", "true"));
			extension.content().inlineComment("nested:options", "options reason");
			extension.attribute("last", "3");
		});
		String written = write(build);
		assertThat(written.indexOf("first = 1")).isLessThan(written.indexOf("// between"));
		assertThat(written.indexOf("// between")).isLessThan(written.indexOf("reset"));
		assertThat(written.indexOf("reset")).isLessThan(written.indexOf("middle = 2"));
		assertThat(written).contains("""
					options {
						enabled = true
					} // options reason
					last = 3
				""");
	}

	@Test
	void anchoredPlacementPreservesLegacyGroupingWithoutInsertionOrder() {
		GradleBuild build = new GradleBuild();
		build.tasks().customize("test", (task) -> {
			task.attribute("enabled", "true");
			task.invoke("reset");
			task.content().before("attribute:enabled").comment("before enabled");
			task.content().inlineComment("attribute:enabled", "reason");
		});
		String written = write(build);
		assertThat(written.indexOf("reset")).isLessThan(written.indexOf("// before enabled"));
		assertThat(written).contains("// before enabled\n\tenabled = true // reason\n");
	}

	@Test
	void taskMovementAndRepeatedCustomizationPreserveContentAndUpdatedAttributes() {
		GradleBuild build = new GradleBuild();
		build.tasks().customize("test", (task) -> {
			task.content().inInsertionOrder();
			task.attribute("first", "1");
			task.attribute("last", "2");
			task.nested("options", (nested) -> nested.content().first().comment("nested first"));
			task.content().moveBefore("nested:options", "attribute:first");
			task.content().after("nested:options").write((writer) -> writer.println("afterOptions = true"));
		});
		build.tasks().customize("test", (task) -> {
			task.attribute("first", "3");
			task.nested("options", (nested) -> nested.attribute("enabled", "true"));
		});
		assertThat(write(build)).contains("""
					options {
						// nested first
						enabled = true
					}
					afterOptions = true
					first = 3
					last = 2
				""");
	}

	@Test
	void repositoryContentCanGenerateCredentialsInTheRepositoryScope() {
		GradleBuild build = new GradleBuild();
		build.repositoryContent("private").first().comment("private repository");
		build.repositoryContent("private").inlineComment("url", "repository URL");
		build.repositoryContent("private").after("url").write((writer) -> {
			writer.println("credentials {");
			writer.indented(() -> {
				writer.println("username = providers.gradleProperty(\"repoUsername\").get()");
				writer.println("password = providers.gradleProperty(\"repoPassword\").get()");
			});
			writer.println("}");
		});
		build.repositories().add(MavenRepository.withIdAndUrl("private", "https://artifacts.example.com"));
		assertThat(write(build)).contains("""
				repositories {
					maven {
						// private repository
				""").contains(" // repository URL\n").contains("""
						credentials {
							username = providers.gradleProperty("repoUsername").get()
							password = providers.gradleProperty("repoPassword").get()
						}
					}
				}
				""");
	}

	@Test
	void repositoryContentUsesTheCurrentUrlAndExpandsCentralShorthand() {
		GradleBuild build = new GradleBuild();
		build.repositories().add("maven-central");
		build.repositoryContent("maven-central").last().raw("name = \"customCentral\"");
		assertThat(write(build)).contains("maven {", "repo.maven.apache.org/maven2", "name = \"customCentral\"")
			.doesNotContain("mavenCentral()");
		build.repositories().add(MavenRepository.withIdAndUrl("maven-central", "https://mirror.example.com"));
		assertThat(write(build)).contains("https://mirror.example.com").doesNotContain("repo.maven.apache.org");
	}

	@Test
	void unregisteredRepositoryContentIsRejected() {
		GradleBuild build = new GradleBuild();
		build.repositoryContent("missing").raw("credentials { }");
		assertThatIllegalArgumentException().isThrownBy(() -> write(build)).withMessageContaining("missing");
	}

	@Test
	void commentsAndRawFragmentsAreWrittenInsideNestedBlocks() {
		GradleBuild build = new GradleBuild();
		build.extensions().customize("custom", (extension) -> extension.nested("options", (nested) -> {
			nested.attribute("enabled", "true");
			nested.comment("First line\r\nSecond line");
			nested.raw("first = 1");
			nested.raw("second = 2");
		}));
		build.tasks().customize("test", (task) -> task.nested("options", (nested) -> {
			nested.comment("Task reason");
			nested.raw("enabled = true");
		}));
		String written = write(build);
		assertThat(written).contains("""
				custom {
					options {
						enabled = true
						// First line
						// Second line
						first = 1
						second = 2
					}
				}
				""");
		assertThat(written).contains("""
					options {
						// Task reason
						enabled = true
					}
				""");
	}

	@Test
	void commentSnippetCanBeMixedWithExistingWriterSnippetAndRaw() {
		GradleBuild build = new GradleBuild();
		build.snippets().comment("Reason");
		build.snippets().add((writer) -> writer.println("first = 1"));
		build.snippets().raw("second = 2");
		assertThat(write(build)).contains("""
				// Reason

				first = 1

				second = 2
				""");
	}

	@Test
	void gradleBuildWithSnippet() {
		GradleBuild build = new GradleBuild();
		build.snippets().add((writer) -> {
			writer.println("custom {");
			writer.indented(() -> {
				writer.println("first = 1");
				writer.println("second = 2");
			});
			writer.println("}");
		});
		assertThat(write(build)).contains("""
				custom {
					first = 1
					second = 2
				}
				""");
	}

	@Test
	void gradleBuildWithSnippetsAreSeparated() {
		GradleBuild build = new GradleBuild();
		build.snippets().add((writer) -> {
			writer.println("custom {");
			writer.indented(() -> {
				writer.println("first = 1");
				writer.println("second = 2");
			});
			writer.println("}");
		});
		build.snippets().add((writer) -> {
			writer.println("another {");
			writer.indented(() -> {
				writer.println("third = 3");
				writer.println("fourth = 4");
			});
			writer.println("}");
		});

		assertThat(write(build)).contains("""
				custom {
					first = 1
					second = 2
				}

				another {
					third = 3
					fourth = 4
				}
				""");
	}

	@Test
	void gradleBuildWithSnippetAndImports() {
		GradleBuild build = new GradleBuild();
		build.snippets().add(Set.of("com.example.CustomTask"), (writer) -> writer.println("custom { }"));
		assertThat(write(build)).containsOnlyOnce("import com.example.CustomTask");
	}

	@Test
	void rawFragmentPreservesInternalWhitespace() {
		GradleBuild build = new GradleBuild();
		build.extensions().customize("custom", (extension) -> extension.raw("text = \"\"\"first\nsecond\n\"\"\""));
		assertThat(write(build)).contains("\ttext = \"\"\"first\nsecond\n\"\"\"\n");
	}

	protected abstract String write(GradleBuild build);

}
