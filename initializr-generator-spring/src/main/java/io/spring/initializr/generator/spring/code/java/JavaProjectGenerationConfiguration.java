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

package io.spring.initializr.generator.spring.code.java;

import io.spring.initializr.generator.condition.ConditionalOnLanguage;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.language.java.JavaSourceCode;
import io.spring.initializr.generator.language.java.JavaSourceCodeWriter;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.code.MainApplicationTypeCustomizer;
import io.spring.initializr.generator.spring.code.MainCompilationUnitCustomizer;
import io.spring.initializr.generator.spring.code.MainSourceCodeCustomizer;
import io.spring.initializr.generator.spring.code.MainSourceCodeProjectContributor;
import io.spring.initializr.generator.spring.code.TestApplicationTypeCustomizer;
import io.spring.initializr.generator.spring.code.TestSourceCodeCustomizer;
import io.spring.initializr.generator.spring.code.TestSourceCodeProjectContributor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Configuration for contributions specific to the generation of a project that will use
 * Java as its language.
 *
 * @author Andy Wilkinson
 */
@ProjectGenerationConfiguration
@ConditionalOnLanguage(JavaLanguage.ID)
@Import(JavaProjectGenerationDefaultContributorsConfiguration.class)
public class JavaProjectGenerationConfiguration {

	private final ProjectDescription description;

	public JavaProjectGenerationConfiguration(ProjectDescription description) {
		this.description = description;
	}

	@Bean
	JavaSourceCodeWriter javaSourceCodeWriter(IndentingWriterFactory indentingWriterFactory) {
		return new JavaSourceCodeWriter(indentingWriterFactory);
	}

	@Bean
	MainSourceCodeProjectContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> mainJavaSourceCodeProjectContributor(
			ObjectProvider<MainApplicationTypeCustomizer<?>> mainApplicationTypeCustomizers,
			ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers,
			ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers,
			JavaSourceCodeWriter javaSourceCodeWriter) {
		return new MainSourceCodeProjectContributor<>(this.description, JavaSourceCode::new, javaSourceCodeWriter,
				mainApplicationTypeCustomizers, mainCompilationUnitCustomizers, mainSourceCodeCustomizers);
	}

	@Bean
	TestSourceCodeProjectContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> testJavaSourceCodeProjectContributor(
			ObjectProvider<TestApplicationTypeCustomizer<?>> testApplicationTypeCustomizers,
			ObjectProvider<TestSourceCodeCustomizer<?, ?, ?>> testSourceCodeCustomizers,
			JavaSourceCodeWriter javaSourceCodeWriter) {
		return new TestSourceCodeProjectContributor<>(this.description, JavaSourceCode::new, javaSourceCodeWriter,
				testApplicationTypeCustomizers, testSourceCodeCustomizers);
	}

}
