package org.misty.test;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

public class StringSplitter extends SimpleArgumentConverter {

    public interface Converter<ResultType> {
        ResultType apply(String source);
    }

    public static class SpecifiedText {
        public static final String NULL = "#null";
    }

    public static final char DEFAULT_SPLITTER = ',';

    private static final Logger LOGGER = LoggerFactory.getLogger(StringSplitter.class);

    private static Map<Class<?>, Converter<Object>> CONVERTERS;

    static {
        resetConverter();
    }

    public static void resetConverter() {
        Map<Class<?>, Converter<Object>> converters = new HashMap<>();

        StringSplitter stringSplitter = new StringSplitter();
        converters.put(String[].class, stringSplitter::convertToArray);
        converters.put(List.class, stringSplitter::convertToList);
        converters.put(Set.class, stringSplitter::convertToSet);

        CONVERTERS = converters;
    }

    @SuppressWarnings("unchecked")
    public static <ResultType> void putConverter(Class<ResultType> resultType, Converter<ResultType> converter) {
        LOGGER.warn("resultType(" + resultType + ") set converter(" + converter.getClass() + ")");
        CONVERTERS.put(resultType, (Converter<Object>) converter);
    }

    public static void removeConverter(Class<?> resultType) {
        Converter<Object> oldConverter = CONVERTERS.remove(resultType);
        if (oldConverter != null) {
            LOGGER.warn("resultType(" + resultType + ") remove converter(" + oldConverter.getClass() + ")");
        }
    }

    public static Set<Class<?>> getConverterHandleTypes() {
        return CONVERTERS.keySet();
    }

    public static void splitWith(String source, Consumer<String> consumer) {
        splitWith(source, DEFAULT_SPLITTER, consumer);
    }

    public static void splitWith(String source, char splitter, Consumer<String> consumer) {
        BiConsumer<Integer, Integer> cut = (index0, index1) -> {
            String text = source.substring(index0, index1);
            consumer.accept(text);
        };

        IntUnaryOperator nextIndex = (fromIndex) -> source.indexOf(splitter, fromIndex);

        int lastIndex = 0;
        int currentIndex = nextIndex.applyAsInt(lastIndex);
        while (currentIndex != -1) {
            cut.accept(lastIndex, currentIndex);
            lastIndex = currentIndex + 1;
            currentIndex = nextIndex.applyAsInt(lastIndex);
        }
        cut.accept(lastIndex, source.length());
    }

    @Override
    protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
        if (source == null) {
            throw new ArgumentConversionException("source can't be null");
        } else if (!(source instanceof String)) {
            throw new ArgumentConversionException("source(" + source.getClass() + ") must be " + String.class);
        }

        Converter<Object> converter = CONVERTERS.getOrDefault(targetType, (key) -> {
            throw new ArgumentConversionException("can't handle targetType(" + targetType + "), please use StringSplitter.putConverter() method to set converter.");
        });
        return converter.apply((String) source);
    }

    private String[] convertToArray(String source) {
        char splitter = DEFAULT_SPLITTER;
        String[] result = new String[(int) source.chars().filter(ch -> ch == splitter).count() + 1];

        AtomicInteger counter = new AtomicInteger();
        splitWith(source, splitter, text -> result[counter.getAndIncrement()] = handleSpecifiedText(text));

        return result;
    }

    private List<String> convertToList(String source) {
        List<String> result = new ArrayList<>(4);
        splitWith(source, text -> result.add(handleSpecifiedText(text)));
        return result;
    }

    private Set<String> convertToSet(String source) {
        Set<String> result = new HashSet<>();
        splitWith(source, text -> result.add(handleSpecifiedText(text)));
        return result;
    }

    private String handleSpecifiedText(String text) {
        if (text.equals(SpecifiedText.NULL)) {
            return null;
        } else {
            return text;
        }
    }

}
