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

import java.io.StringWriter;
import java.util.Comparator;

import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.generator.io.SimpleIndentStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for shared content ordering and placement independent of target syntax.
 *
 * @author Sijun Yang
 */
class ContentSequenceTests {

	@Test
	void anchoredContentPreservesDefaultOrderAndRegistrationOrder() {
		ContentSequence.Builder<String> builder = new ContentSequence.Builder<>();
		builder.before("a").comment("before a");
		builder.after("a").raw("after a 1").raw("after a 2");
		builder.add("b", "b");
		builder.add("a", "a");
		assertThat(write(builder.build(Comparator.naturalOrder()))).isEqualTo("""
				block {
					# before a
					a
					after a 1
					after a 2
					b
				}
				""");
	}

	@Test
	void unanchoredContentSelectsInsertionOrderAndCallbacksUseCurrentIndentation() {
		ContentSequence.Builder<String> builder = new ContentSequence.Builder<>();
		builder.add("b", "b");
		builder.comment("between");
		builder.write((writer) -> {
			writer.println("custom {");
			writer.indented(() -> writer.println("value"));
			writer.println("}");
		});
		builder.add("a", "a");
		assertThat(write(builder.build(Comparator.naturalOrder()))).isEqualTo("""
				block {
					b
					# between
					custom {
						value
					}
					a
				}
				""");
	}

	@Test
	void explicitInsertionOrderOverridesDefaultOrder() {
		ContentSequence.Builder<String> builder = new ContentSequence.Builder<>();
		builder.inInsertionOrder();
		builder.add("b", "b");
		builder.add("a", "a");
		assertThat(builder.build(Comparator.naturalOrder()).elements()).containsExactly("b", "a");
	}

	@Test
	void boundariesRemainAtBlockBoundariesWhenElementsAreAddedLater() {
		ContentSequence.Builder<String> builder = new ContentSequence.Builder<>();
		builder.first().comment("first");
		builder.last().comment("last");
		builder.add("a", "a");
		builder.add("b", "b");
		assertThat(write(builder.build())).isEqualTo("""
				block {
					# first
					a
					b
					# last
				}
				""");
	}

	@Test
	void movementAndReplacementRetainAttachedContent() {
		ContentSequence.Builder<String> builder = new ContentSequence.Builder<>();
		builder.add("a", "a");
		builder.add("b", "old b");
		builder.add("c", "c");
		builder.before("b").comment("before b");
		builder.inlineComment("b", "reason");
		builder.put("b", "new b");
		builder.moveBefore("b", "a");
		builder.moveAfter("c", "b");
		assertThat(write(builder.build())).isEqualTo("""
				block {
					# before b
					new b # reason
					c
					a
				}
				""");
	}

	@Test
	void repeatedKeysCanBeTargetedByOccurrence() {
		ContentSequence.Builder<String> builder = new ContentSequence.Builder<>();
		builder.add("flag", "first");
		builder.add("flag", "second");
		builder.before("flag", 1).comment("before second");
		builder.inlineComment("flag", 1, "second reason");
		builder.after("flag", 1).comment("after second");
		assertThat(write(builder.build())).isEqualTo("""
				block {
					first
					# before second
					second # second reason
					# after second
				}
				""");
	}

	@Test
	void snapshotIsImmutableAndMappingPreservesPlacement() {
		ContentSequence.Builder<String> builder = new ContentSequence.Builder<>();
		builder.add("a", "a");
		builder.inlineComment("a", "old");
		ContentSequence<String> snapshot = builder.build();
		builder.put("a", "changed");
		builder.inlineComment("a", "new");
		assertThat(write(snapshot.map(String::toUpperCase))).contains("A # old").doesNotContain("changed", "new");
		assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> snapshot.entries().clear());
		ContentSequence.Element<String> element = (ContentSequence.Element<String>) snapshot.entries().get(0);
		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> element.inlineComments().clear());
	}

	@Test
	void missingAnchorsAndInvalidOccurrencesAreRejected() {
		ContentSequence.Builder<String> builder = new ContentSequence.Builder<>();
		builder.before("missing").comment("reason");
		assertThatIllegalArgumentException().isThrownBy(builder::build).withMessageContaining("missing");
		ContentSequence.Builder<String> repeated = new ContentSequence.Builder<>();
		repeated.add("flag", "first");
		repeated.inlineComment("flag", 1, "missing second");
		assertThatIllegalArgumentException().isThrownBy(repeated::build).withMessageContaining("occurrence 1");
		assertThatIllegalArgumentException().isThrownBy(() -> repeated.before("flag", -1));
	}

	@Test
	void missingMovementDestinationAndMultilineInlineCommentsAreRejected() {
		ContentSequence.Builder<String> builder = new ContentSequence.Builder<>();
		builder.add("a", "a");
		builder.moveAfter("a", "missing");
		assertThatIllegalArgumentException().isThrownBy(builder::build).withMessageContaining("missing");
		assertThatIllegalArgumentException().isThrownBy(() -> builder.inlineComment("a", "one\ntwo"));
		assertThatIllegalArgumentException().isThrownBy(() -> builder.inlineComment("a", "one\rtwo"));
	}

	private String write(ContentSequence<String> sequence) {
		StringWriter out = new StringWriter();
		IndentingWriter writer = new IndentingWriter(out, new SimpleIndentStrategy("\t"));
		ContentWriter contentWriter = new ContentWriter() {
			@Override
			public String inlineComment(String text) {
				return "# " + text;
			}

			@Override
			public void writeFragment(IndentingWriter target, BuildFragment fragment) {
				target.println((fragment.kind() == BuildFragment.Kind.COMMENT) ? inlineComment(fragment.content())
						: fragment.content());
			}
		};
		writer.println("block {");
		writer.indented(() -> contentWriter.write(writer, sequence, (target, value) -> {
			target.print(value);
			return true;
		}));
		writer.println("}");
		return out.toString().replace("\r\n", "\n");
	}

}
