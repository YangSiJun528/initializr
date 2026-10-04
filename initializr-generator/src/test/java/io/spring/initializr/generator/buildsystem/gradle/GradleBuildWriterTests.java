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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common tests for {@link GradleBuildWriter} implementations.
 *
 * @author Stephane Nicoll
 * @author Sijun Yang
 */
public abstract class GradleBuildWriterTests {

	@Test
	void extensionContentPreservesExistingOrderAndSupportsNestedCallbacks() {
		GradleBuild build = new GradleBuild();
		build.extensions().customize("custom", (extension) -> {
			extension.attribute("enabled", "true");
			extension.invoke("configure", "42");
			extension.content().first().comment("Reason\nReference");
			extension.content().before("attribute:enabled").raw("customOption = 1");
			extension.nested("options", (nested) -> {
				nested.attribute("count", "2");
				nested.content().after("attribute:count").write((writer) -> {
					writer.println("extra {");
					writer.indented(() -> writer.println("flag = true"));
					writer.println("}");
				});
			});
			extension.content().after("nested:options").comment("End of options");
		});
		String written = write(build);
		assertThat(written).contains("// Reason\n\t// Reference\n");
		assertThat(written.indexOf("configure")).isLessThan(written.indexOf("customOption = 1"));
		assertThat(written).contains("""
					customOption = 1
					enabled = true
					options {
						count = 2
						extra {
							flag = true
						}
					}
					// End of options
				""");
	}

	@Test
	void taskContentUsesCurrentAttributeValueAndPreservesDefaultGrouping() {
		GradleBuild build = new GradleBuild();
		build.tasks().customize("custom", (task) -> {
			task.attribute("enabled", "false");
			task.content().before("attribute:enabled").comment("Current value");
			task.attribute("enabled", "true");
			task.invoke("configure", "42");
			task.content().last().raw("extra = 2");
		});
		String written = write(build);
		assertThat(written.indexOf("configure")).isLessThan(written.indexOf("// Current value"));
		assertThat(written).contains("""
					// Current value
					enabled = true
					extra = 2
				""").doesNotContain("enabled = false");
	}

	@Test
	void legacyExtensionGetterOverridesArePreserved() {
		GradleExtension nested = new GradleExtension(new GradleExtension.Builder("options")) {
			@Override
			public List<Attribute> getAttributes() {
				return List.of(Attribute.set("childValue", "2"));
			}
		};
		GradleExtension.Builder builder = new GradleExtension.Builder("legacy");
		builder.attribute("ignored", "false");
		GradleExtension extension = new GradleExtension(builder) {
			@Override
			public List<Attribute> getAttributes() {
				return List.of(new Attribute("enabled", "false", Attribute.Type.SET) {
					@Override
					public String getValue() {
						return "true";
					}
				});
			}

			@Override
			public List<Invocation> getInvocations() {
				return List.of(new Invocation("configure", List.of("0")) {
					@Override
					public List<String> getArguments() {
						return List.of("42");
					}
				});
			}

			@Override
			public Map<String, GradleExtension> getNested() {
				return Map.of("mapKey", nested);
			}
		};
		GradleBuild build = new GradleBuild() {
			@Override
			public GradleExtensionContainer extensions() {
				return new GradleExtensionContainer() {
					@Override
					public Stream<GradleExtension> values() {
						return Stream.of(extension);
					}
				};
			}
		};
		String written = write(build);
		assertThat(written).containsPattern("configure(?:\\(42\\)| 42)");
		assertThat(written).contains("""
					enabled = true
					options {
						childValue = 2
					}
				""").doesNotContain("ignored = false");
	}

	@Test
	@SuppressWarnings("removal")
	void legacyTaskGetterOverridesPreserveNestedPropertyNamesAndArguments() {
		GradleTask nested = new GradleTask(new GradleTask.Builder("ignoredChildName")) {
			@Override
			public List<GradleTask.Attribute> getAttributes() {
				return List.of(GradleTask.Attribute.set("childValue", "2"));
			}
		};
		GradleTask task = new GradleTask(new GradleTask.Builder("legacy")) {
			@Override
			public List<GradleTask.Attribute> getAttributes() {
				return List.of(GradleTask.Attribute.set("enabled", "true"));
			}

			@Override
			public List<GradleTask.Invocation> getInvocations() {
				return List.of(new GradleTask.Invocation("configure", List.of("0")) {
					@Override
					public List<String> getArguments() {
						return List.of("42");
					}
				});
			}

			@Override
			public Map<String, GradleTask> getNested() {
				return Map.of("options", nested);
			}
		};
		GradleBuild build = new GradleBuild() {
			@Override
			public GradleTaskContainer tasks() {
				return new GradleTaskContainer() {
					@Override
					public Stream<GradleTask> values() {
						return Stream.of(task);
					}
				};
			}
		};
		String written = write(build);
		assertThat(written).containsPattern("configure(?:\\(42\\)| 42)");
		assertThat(written).contains("""
					enabled = true
					options {
						childValue = 2
					}
				""").doesNotContain("ignoredChildName");
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

	protected abstract String write(GradleBuild build);

}
