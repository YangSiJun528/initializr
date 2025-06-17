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

package io.spring.initializr.generator.language;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeDeclaration}.
 *
 * @author Moritz Halbritter
 */
class TypeDeclarationTests {

	@Test
	void implementWithVarArgs() {
		TypeDeclaration declaration = new TypeDeclaration("Test");
		declaration.implement("Interface1", "Interface2");
		assertThat(declaration.getImplements()).containsExactly("Interface1", "Interface2");
	}

}
