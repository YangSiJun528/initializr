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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Specialization of {@link Configuration} for configuration of project generation, with
 * {@link Configuration#proxyBeanMethods()} set to {@code false} by default.
 * <p>
 * Project generation configuration classes are regular Spring {@link Configuration}
 * beans. They are located using the {@link SpringFactoriesLoader} mechanism (keyed
 * against this class). Project generation beans can be {@link Conditional @Conditional}
 * beans, usually based on the state of the {@link ProjectDescription} for which the
 * {@link ProjectGenerationContext} was created.
 * <p>
 * {@link ProjectGenerationConfiguration @ProjectGenerationConfiguration}-annotated types
 * should not be processed by the main {@link ApplicationContext} so make sure regular
 * classpath scanning is not enabled for packages where such configuration classes reside.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface ProjectGenerationConfiguration {

	/**
	 * Specify whether {@code @Bean} methods should get proxied in order to enforce bean
	 * lifecycle behavior, e.g. to return shared singleton bean instances even in case of
	 * direct {@code @Bean} method calls in user code.
	 * @see Configuration#proxyBeanMethods()
	 * @return whether a CGLIB proxy should be created to enforce bean lifecycle behavior
	 */
	@AliasFor(annotation = Configuration.class)
	boolean proxyBeanMethods() default false;

}
