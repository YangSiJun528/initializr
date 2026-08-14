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

package io.spring.initializr.generator.spring.documentation;

import io.spring.initializr.generator.io.template.MustacheTemplateRenderer;
import io.spring.initializr.generator.project.MutableProjectDescription;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectDescriptionField;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ProjectDescriptionChangesHelpDocumentCustomizer}.
 *
 * @author Moritz Halbritter
 */
class ProjectDescriptionChangesHelpDocumentCustomizerTests {

	private final HelpDocument document = new HelpDocument(mock(MustacheTemplateRenderer.class));

	private final MutableProjectDescription description = new MutableProjectDescription();

	@Test
	void recordedReasonIsAddedAsWarning() {
		this.description.getChanges().add(ProjectDescriptionField.JVM_VERSION, "test reason");
		customize(this.description);
		assertThat(this.document.getWarnings().getItems()).containsExactly("test reason");
	}

	@Test
	void onlyTheLastRecordedReasonOfAFieldIsAddedAsWarning() {
		this.description.getChanges().add(ProjectDescriptionField.JVM_VERSION, "first reason");
		this.description.getChanges().add(ProjectDescriptionField.JVM_VERSION, "second reason");
		customize(this.description);
		assertThat(this.document.getWarnings().getItems()).containsExactly("second reason");
	}

	@Test
	void documentIsEmptyWhenNoReasonIsRecorded() {
		customize(this.description);
		assertThat(this.document.getWarnings().getItems()).isEmpty();
		assertThat(this.document.isEmpty()).isTrue();
	}

	@Test
	void documentIsEmptyWithNonMutableDescription() {
		customize(mock(ProjectDescription.class));
		assertThat(this.document.isEmpty()).isTrue();
	}

	private void customize(ProjectDescription description) {
		new ProjectDescriptionChangesHelpDocumentCustomizer(description).customize(this.document);
	}

}
