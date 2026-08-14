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

package io.spring.initializr.web.controller;

import io.spring.initializr.web.AbstractInitializrControllerIntegrationTests;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Integration tests for a service that restricts the platform versions it supports to a
 * subset of the available versions.
 *
 * @author Moritz Halbritter
 */
@ActiveProfiles({ "test-default", "test-platform-range" })
class PlatformVersionRangeIntegrationTests extends AbstractInitializrControllerIntegrationTests {

	@Test
	void generateProjectWithWildcardPlatformVersionOutOfRange() {
		assertThatExceptionOfType(HttpClientErrorException.class)
			.isThrownBy(() -> execute("/starter.tgz?bootVersion=2.3.x", byte[].class, null, (String[]) null))
			.satisfies((ex) -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(ex.getResponseBodyAsString()).contains("Invalid Spring Boot version", "2.3.10.RELEASE",
						"Spring Boot compatibility range is");
			});
	}

	@Test
	void getDependenciesWithWildcardPlatformVersionOutOfRange() {
		assertThatExceptionOfType(HttpClientErrorException.class)
			.isThrownBy(() -> execute("/dependencies?bootVersion=2.3.x", String.class, null,
					"application/vnd.initializr.v2.1+json"))
			.satisfies((ex) -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				// The resolved version is reported, not the wildcard that was requested
				assertThat(ex.getResponseBodyAsString())
					.contains("\"message\":\"Invalid Spring Boot version '2.3.10.RELEASE', "
							+ "Spring Boot compatibility range is >=2.4.0 and <2.5.0-SNAPSHOT\"");
			});
	}

}
