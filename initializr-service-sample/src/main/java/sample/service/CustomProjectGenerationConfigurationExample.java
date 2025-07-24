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

package sample.service;

import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.web.controller.ProjectGenerationController;
import io.spring.initializr.web.project.DefaultProjectRequestToDescriptionConverter;
import io.spring.initializr.web.project.ProjectGenerationInvoker;
import io.spring.initializr.web.project.ProjectRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration example of a custom {@link ProjectGenerationController}.
 *
 * @author Stephane Nicoll
 */
@Configuration
public class CustomProjectGenerationConfigurationExample {

	@Bean
	public CustomProjectGenerationController projectGenerationController(InitializrMetadataProvider metadataProvider,
			ApplicationContext applicationContext) {
		ProjectGenerationInvoker<ProjectRequest> projectGenerationInvoker = new ProjectGenerationInvoker<>(
				applicationContext, new DefaultProjectRequestToDescriptionConverter());
		return new CustomProjectGenerationController(metadataProvider, projectGenerationInvoker);
	}

}
