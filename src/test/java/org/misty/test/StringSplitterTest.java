package org.misty.test;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

class StringSplitterTest {

    private static final Map<String, AtomicInteger> COUNTER_MAP = new HashMap<>();

    @ParameterizedTest
    @ValueSource(strings = {
            ",e", // 0
            "e,", // 1
            "e,#null", // 2
            "#null,e", // 3
            "e,,e", // 4
            "e,null,e" // 5
    })
    void convertToArray(@ConvertWith(StringSplitter.class) String[] targets) {
        int index = 0;

        int times = COUNTER_MAP.computeIfAbsent("convertToArray", k -> new AtomicInteger()).getAndIncrement();
        if (times == 0) {
            Assertions.assertThat(targets[index++]).isEqualTo("");
            Assertions.assertThat(targets[index++]).isEqualTo("e");

        } else if (times == 1) {
            Assertions.assertThat(targets[index++]).isEqualTo("e");
            Assertions.assertThat(targets[index++]).isEqualTo("");

        } else if (times == 2) {
            Assertions.assertThat(targets[index++]).isEqualTo("e");
            Assertions.assertThat(targets[index++]).isEqualTo(null);

        } else if (times == 3) {
            Assertions.assertThat(targets[index++]).isEqualTo(null);
            Assertions.assertThat(targets[index++]).isEqualTo("e");

        } else if (times == 4) {
            Assertions.assertThat(targets[index++]).isEqualTo("e");
            Assertions.assertThat(targets[index++]).isEqualTo("");
            Assertions.assertThat(targets[index++]).isEqualTo("e");

        } else if (times == 5) {
            Assertions.assertThat(targets[index++]).isEqualTo("e");
            Assertions.assertThat(targets[index++]).isEqualTo("null");
            Assertions.assertThat(targets[index++]).isEqualTo("e");

        } else {
            throw new IllegalStateException();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ",e", // 0
            "e,", // 1
            "e,#null", // 2
            "#null,e", // 3
            "e,,e", // 4
            "e,null,e" // 5
    })
    void convertToList(@ConvertWith(StringSplitter.class) List<String> targets) {
        int times = COUNTER_MAP.computeIfAbsent("convertToList", k -> new AtomicInteger()).getAndIncrement();
        if (times == 0) {
            Assertions.assertThat(targets).containsExactly("", "e");

        } else if (times == 1) {
            Assertions.assertThat(targets).containsExactly("e", "");

        } else if (times == 2) {
            Assertions.assertThat(targets).containsExactly("e", null);

        } else if (times == 3) {
            Assertions.assertThat(targets).containsExactly(null, "e");

        } else if (times == 4) {
            Assertions.assertThat(targets).containsExactly("e", "", "e");

        } else if (times == 5) {
            Assertions.assertThat(targets).containsExactly("e", "null", "e");

        } else {
            throw new IllegalStateException();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ",e", // 0
            "e,", // 1
            "e,#null", // 2
            "#null,e", // 3
            "e,,e", // 4
            "e,null,e" // 5
    })
    void convertToSet(@ConvertWith(StringSplitter.class) Set<String> targets) {
        int times = COUNTER_MAP.computeIfAbsent("convertToSet", k -> new AtomicInteger()).getAndIncrement();
        if (times == 0) {
            Assertions.assertThat(targets).containsExactlyInAnyOrder("", "e");

        } else if (times == 1) {
            Assertions.assertThat(targets).containsExactlyInAnyOrder("e", "");

        } else if (times == 2) {
            Assertions.assertThat(targets).containsExactlyInAnyOrder("e", null);

        } else if (times == 3) {
            Assertions.assertThat(targets).containsExactlyInAnyOrder(null, "e");

        } else if (times == 4) {
            Assertions.assertThat(targets).containsExactlyInAnyOrder("e", "");

        } else if (times == 5) {
            Assertions.assertThat(targets).containsExactlyInAnyOrder("e", "null");

        } else {
            throw new IllegalStateException();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void modify_converter() {
        Class<String> targetType = String.class;

        // putConverter
        String testTarget = "9527";
        StringSplitter.putConverter(targetType, (source) -> testTarget);
        AssertionsEx.assertThat(new StringSplitter().convert("kerker", targetType)).isEqualTo(testTarget);
        AssertionsEx.assertThat(StringSplitter.getConverterHandleTypes())
                .contains(String[].class, List.class, Set.class, String.class);

        // removeConverter
        StringSplitter.removeConverter(targetType);
        AssertionsEx.asserAwareThrown(() -> new StringSplitter().convert("kerker", targetType))
                .isInstanceOf(ArgumentConversionException.class);
        AssertionsEx.assertThat(StringSplitter.getConverterHandleTypes())
                .contains(String[].class, List.class, Set.class);

        // reset
        Class<Date> targetType1 = Date.class;
        Class<Map> targetType2 = Map.class;
        StringSplitter.putConverter(targetType1, (source) -> null);
        StringSplitter.putConverter(targetType2, (source) -> null);
        AssertionsEx.assertThat(StringSplitter.getConverterHandleTypes())
                .contains(String[].class, List.class, Set.class, targetType1, targetType2);

        StringSplitter.resetConverter();

        AssertionsEx.assertThat(StringSplitter.getConverterHandleTypes())
                .contains(String[].class, List.class, Set.class);
    }

    @Test
    void splitWith() {
        char splitter = ',';

        BiConsumer<String, List<String>> test = (source, tester) -> {
            List<String> result = new ArrayList<>();
            StringSplitter.splitWith(source, splitter, result::add);
            AssertionsEx.assertThat(result).containsAll(tester);
        };

        test.accept("", Collections.singletonList(""));
        test.accept("a", Collections.singletonList("a"));
        test.accept("a,b", Arrays.asList("a", "b"));
        test.accept("a,b,, ", Arrays.asList("a", "b", "", " "));
        test.accept(",a,b,     ", Arrays.asList("", "a", "b", "     "));
    }

}
