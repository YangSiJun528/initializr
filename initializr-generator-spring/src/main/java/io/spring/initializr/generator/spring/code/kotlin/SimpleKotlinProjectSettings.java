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

package io.spring.initializr.generator.spring.code.kotlin;

import java.util.ArrayList;
import java.util.List;

import io.spring.initializr.generator.language.Language;
import io.spring.initializr.generator.version.Version;

/**
 * Commons settings for Kotlin projects.
 *
 * @author Andy Wilkinson
 * @author Moritz Halbritter
 */
public class SimpleKotlinProjectSettings implements KotlinProjectSettings {

	private static final Version KOTLIN_2_2_OR_LATER = Version.parse("2.2.0");

	private final String version;

	private final String jvmTarget;

	/**
	 * Create an instance with the kotlin version to use.
	 * @param version the kotlin version to use
	 */
	public SimpleKotlinProjectSettings(String version) {
		this(version, Language.DEFAULT_JVM_VERSION);
	}

	/**
	 * Create an instance with the kotlin version and the target version of the generated
	 * JVM bytecode.
	 * @param version the kotlin version to use
	 * @param jvmTarget the target version of the generated JVM bytecode
	 */
	public SimpleKotlinProjectSettings(String version, String jvmTarget) {
		this.version = version;
		this.jvmTarget = jvmTarget;
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	@Override
	public String getJvmTarget() {
		return this.jvmTarget;
	}

	@Override
	public List<String> getCompilerArgs() {
		List<String> result = new ArrayList<>(KotlinProjectSettings.super.getCompilerArgs());
		Version kotlinVersion = Version.parse(this.version);
		if (kotlinVersion.compareTo(KOTLIN_2_2_OR_LATER) >= 0) {
			result.add("-Xannotation-default-target=param-property");
		}
		return result;
	}

}
