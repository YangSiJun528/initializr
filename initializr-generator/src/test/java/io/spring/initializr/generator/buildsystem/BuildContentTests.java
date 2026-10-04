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

package io.spring.initializr.generator.buildsystem;

import java.io.StringWriter;
import java.util.List;

import io.spring.initializr.generator.io.IndentingWriter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for positioned contributions to existing build elements.
 *
 * @author Sijun Yang
 */
class BuildContentTests {

	@Test
	void contributionsPreserveElementOrderAndSelectOnlyTheFirstRepeatedKey() {
		BuildContent.Builder builder = new BuildContent.Builder();
		builder.first().comment("start");
		builder.before("item").raw("before").raw("second");
		builder.after("item").write((writer) -> writer.println("after"));
		builder.last().comment("end");
		assertThat(write(builder.build(), List.of("other", "item", "item"))).isEqualTo("""
				// start
				other
				before
				second
				item
				after
				item
				// end
				""");
	}

	@Test
	void snapshotsRetainRawWhitespaceAndIgnoreSubsequentChanges() {
		BuildContent.Builder builder = new BuildContent.Builder();
		builder.last().raw("<![CDATA[first\n  second\n]]>");
		BuildContent snapshot = builder.build();
		builder.last().comment("later");
		assertThat(write(snapshot, List.of())).isEqualTo("<![CDATA[first\n  second\n]]>\n");
		assertThat(write(builder.build(), List.of())).endsWith("// later\n");
	}

	@Test
	void missingAnchorIsRejectedBeforeWritingElementsOrOtherContributions() {
		BuildContent.Builder builder = new BuildContent.Builder();
		builder.first().raw("start");
		builder.after("missing").comment("reason");
		StringWriter out = new StringWriter();
		assertThatIllegalArgumentException()
			.isThrownBy(() -> builder.build()
				.writeTo(new IndentingWriter(out), List.of("item"), (item) -> item, IndentingWriter::println,
						(writer, text) -> writer.println(text)))
			.withMessageContaining("missing");
		assertThat(out.toString()).isEmpty();
	}

	private String write(BuildContent content, List<String> elements) {
		StringWriter out = new StringWriter();
		content.writeTo(new IndentingWriter(out), elements, (item) -> item, IndentingWriter::println,
				(writer, text) -> writer.println("// " + text));
		return out.toString().replace("\r\n", "\n");
	}

}
