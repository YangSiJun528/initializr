package io.spring.initializr.generator.language;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MultipleAnnotationContainerTests {

    private static final ClassName TEST_CLASS_NAME = ClassName.of("com.example.Test");

    private static final ClassName OTHER_CLASS_NAME = ClassName.of("com.example.Other");

    @Test
    void isEmptyWithEmptyContainer() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        assertThat(container.isEmpty()).isTrue();
    }

    @Test
    void isEmptyWithAnnotation() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME, builder -> builder.add("value", "test"));
        assertThat(container.isEmpty()).isFalse();
    }

    @Test
    void hasWithMatchingAnnotation() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME, builder -> builder.add("value", "test"));
        assertThat(container.has(TEST_CLASS_NAME)).isTrue();
    }

    @Test
    void hasWithNonMatchingAnnotation() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME, builder -> builder.add("value", "test"));
        assertThat(container.has(OTHER_CLASS_NAME)).isFalse();
    }

    @Test
    void valuesShouldReturnAllAnnotations() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME, builder -> builder.add("value", "one"));
        container.addToList(TEST_CLASS_NAME, builder -> builder.add("value", "two"));
        List<Annotation> annotations = container.values().collect(Collectors.toList());
        assertThat(annotations).hasSize(2);
        assertThat(annotations).allMatch(annotation -> annotation.getClassName().equals(TEST_CLASS_NAME));
    }

    @Test
    void valuesOfShouldReturnMatchingAnnotationsOnly() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME, builder -> builder.add("value", "one"));
        container.addToList(OTHER_CLASS_NAME, builder -> builder.add("name", "other"));
        List<Annotation> annotations = container.valuesOf(TEST_CLASS_NAME).collect(Collectors.toList());
        assertThat(annotations).hasSize(1);
        assertThat(annotations.get(0).getAttributes().get(0).getValues()).containsExactly("one");
    }

    @Test
    void countOfShouldReturnCorrectCount() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME);
        container.addToList(TEST_CLASS_NAME);
        assertThat(container.countOf(TEST_CLASS_NAME)).isEqualTo(2);
        assertThat(container.countOf(OTHER_CLASS_NAME)).isZero();
    }

    @Test
    void removeAllShouldRemoveAllAnnotationsOfClassName() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME);
        container.addToList(TEST_CLASS_NAME);
        int removed = container.removeAll(TEST_CLASS_NAME);
        assertThat(removed).isEqualTo(2);
        assertThat(container.has(TEST_CLASS_NAME)).isFalse();
    }

    @Test
    void removeAtShouldRemoveSpecificAnnotation() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME, b -> b.add("value", "first"));
        container.addToList(TEST_CLASS_NAME, b -> b.add("value", "second"));
        boolean result = container.removeAt(TEST_CLASS_NAME, 0);
        assertThat(result).isTrue();
        List<Annotation> remaining = container.valuesOf(TEST_CLASS_NAME).collect(Collectors.toList());
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getAttributes().get(0).getValues()).containsExactly("second");
    }

    @Test
    void removeAtWithInvalidIndexReturnsFalse() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME);
        assertThat(container.removeAt(TEST_CLASS_NAME, 5)).isFalse();
        assertThat(container.removeAt(OTHER_CLASS_NAME, 0)).isFalse();
    }

    @Test
    void hasMultipleReturnsTrueForMultipleAnnotations() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME);
        container.addToList(TEST_CLASS_NAME);
        assertThat(container.hasMultiple(TEST_CLASS_NAME)).isTrue();
        assertThat(container.hasMultiple(OTHER_CLASS_NAME)).isFalse();
    }

    @Test
    void addUnsupportedMethodsThrowException() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        assertThatThrownBy(() -> container.add(TEST_CLASS_NAME))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> container.add(TEST_CLASS_NAME, builder -> {}))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void deepCopyShouldCopyAllAnnotationsIndependently() {
        MultipleAnnotationContainer container = new MultipleAnnotationContainer();
        container.addToList(TEST_CLASS_NAME, builder -> builder.add("value", "test"));
        MultipleAnnotationContainer copy = container.deepCopy();

        // Mutate original
        container.addToList(TEST_CLASS_NAME, builder -> builder.add("value", "another"));

        assertThat(copy.countOf(TEST_CLASS_NAME)).isEqualTo(1);
        assertThat(container.countOf(TEST_CLASS_NAME)).isEqualTo(2);
    }
}
