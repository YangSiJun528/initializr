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

package io.spring.initializr.generator.project;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link ProjectDescriptionChanges}.
 *
 * @author Moritz Halbritter
 */
class ProjectDescriptionChangesTests {

	private final ProjectDescriptionChanges changes = new ProjectDescriptionChanges();

	@Test
	void emptyByDefault() {
		assertThat(this.changes.isEmpty()).isTrue();
		assertThat(this.changes.getAll()).isEmpty();
		assertThat(this.changes.get(ProjectDescriptionField.JVM_VERSION)).isNull();
	}

	@Test
	void addRecordsChange() {
		this.changes.add(ProjectDescriptionField.JVM_VERSION, "test reason");
		assertThat(this.changes.isEmpty()).isFalse();
		assertThat(this.changes.getAll())
			.containsExactly(new ProjectDescriptionChange(ProjectDescriptionField.JVM_VERSION, "test reason"));
	}

	@Test
	void addKeepsOnlyTheLastReasonOfAField() {
		this.changes.add(ProjectDescriptionField.JVM_VERSION, "first");
		this.changes.add(ProjectDescriptionField.JVM_VERSION, "second");
		assertThat(this.changes.getAll()).extracting(ProjectDescriptionChange::getReason).containsExactly("second");
		assertThat(this.changes.get(ProjectDescriptionField.JVM_VERSION)).isNotNull()
			.extracting(ProjectDescriptionChange::getReason)
			.isEqualTo("second");
	}

	@Test
	void getAllReturnsUnmodifiableList() {
		this.changes.add(ProjectDescriptionField.JVM_VERSION, "test reason");
		assertThat(this.changes.getAll()).isUnmodifiable();
	}

	@Test
	void addWithEmptyReasonIsRejected() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.changes.add(ProjectDescriptionField.JVM_VERSION, ""))
			.withMessage("'reason' must not be empty");
	}

}
