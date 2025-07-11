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

package io.spring.initializr.generator.spring.build.gradle;

import io.spring.initializr.generator.project.ProjectDescription;

/**
 * Strategy for resolving a dependency management plugin version from a platform version.
 *
 * @author Stephane Nicoll
 */
@FunctionalInterface
public interface DependencyManagementPluginVersionResolver {

	/**
	 * Resolves the dependency management plugin version to use for the generation of the
	 * project with the given {@code description}.
	 * @param description the description of the project being generated
	 * @return the corresponding version for the {@code io.spring.dependency-management}
	 * plugin
	 */
	String resolveDependencyManagementPluginVersion(ProjectDescription description);

}
