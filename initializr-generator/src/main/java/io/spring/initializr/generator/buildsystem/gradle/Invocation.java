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

import io.spring.initializr.generator.buildsystem.content.BuildValue;

/**
 * An invocation of a method.
 *
 * @author Moritz Halbritter
 * @author Stephane Nicoll
 * @author Sijun Yang
 */
public class Invocation {

	private final String target;

	private final List<BuildValue> arguments;

	/**
	 * Creates a new instance.
	 * @param target the target
	 * @param arguments the arguments
	 */
	public Invocation(String target, List<String> arguments) {
		this.target = target;
		this.arguments = arguments.stream().map(BuildValue::raw).toList();
	}

	/**
	 * Create an invocation with explicit text or raw arguments.
	 * @param target the target
	 * @param arguments the arguments
	 */
	public Invocation(String target, BuildValue... arguments) {
		this.target = target;
		this.arguments = List.of(arguments);
	}

	/**
	 * Return the name of the method.
	 * @return the method name
	 */
	public String getTarget() {
		return this.target;
	}

	/**
	 * Return the arguments (can be empty).
	 * @return the method arguments
	 */
	public List<String> getArguments() {
		return this.arguments.stream().map(BuildValue::content).toList();
	}

	List<BuildValue> getArgumentValues() {
		if (this.arguments.stream().allMatch((value) -> value.kind() == BuildValue.Kind.RAW)) {
			return getArguments().stream().map(BuildValue::raw).toList();
		}
		return this.arguments;
	}

}
