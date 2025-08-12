package io.spring.initializr.generator.spring.build.gradle;

import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.project.MutableProjectDescription;
import io.spring.initializr.generator.project.ProjectDescription;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GradleSettingBuildCustomizerTests {

    private ProjectDescription projectDescription;

    @Test
    void customizeShouldSetGradleDescriptionWhenPresent() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setDescription("Test project description");
        this.projectDescription = description;
        GradleBuild build = new GradleBuild();

        new GradleSettingBuildCustomizer(this.projectDescription).customize(build);

        assertThat(build.getSettings().getDescription()).isEqualTo("Test project description");
    }

    @Test
    void customizeShouldNotSetDescriptionWhenBlank() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setDescription("   ");
        this.projectDescription = description;
        GradleBuild build = new GradleBuild();

        new GradleSettingBuildCustomizer(this.projectDescription).customize(build);

        assertThat(build.getSettings().getDescription()).isNull();
    }

    @Test
    void customizeShouldNotSetDescriptionWhenNull() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setDescription(null);
        this.projectDescription = description;
        GradleBuild build = new GradleBuild();

        new GradleSettingBuildCustomizer(this.projectDescription).customize(build);

        assertThat(build.getSettings().getDescription()).isNull();
    }
}
