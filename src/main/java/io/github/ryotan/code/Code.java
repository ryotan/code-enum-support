/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.ryotan.code.CodeEnum.Filters;
import io.github.ryotan.code.CodeEnum.OptionalLabel;
import io.github.ryotan.code.CodeEnum.ShortLabel;
import io.github.ryotan.code.util.CodeEnumReflectionUtil;

public final class Code {

    private static final Comparator<CodeEnum<?>> ORDINAL_COMPARATOR = (c1, c2) -> c1.ordinal() - c2.ordinal();

    private Code() {
    }

    private static boolean matches(CodeEnum<?> candidate, String value) {
        return candidate.value().equals(value);
    }

    private static <C extends CodeEnum<C>> Stream<C> enums(Class<C> code) {
        if (code.isEnum()) {
            return Stream.of(code.getEnumConstants());
        }
        throw new IllegalArgumentException(String.format("CodeEnum must be enum. class=[%s].", code.getName()));
    }

    public static <C extends CodeEnum<C>> C of(Class<C> code, String value) {
        return of(code, value, Filters.ANY);
    }

    public static <C extends CodeEnum<C>> C of(Class<C> code, String value, Predicate<? super C> filter) {
        return enums(code).filter(filter).filter(c -> matches(c, value)).findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Code is not found. code=[%s], value=[%s]", code.getName(), value)));
    }

    public static <C extends CodeEnum<C>> C or(Class<C> code, String value, C defaultCode) {
        return or(code, value, defaultCode, Filters.ANY);
    }

    public static <C extends CodeEnum<C>> C or(Class<C> code, String value, C defaultCode, Predicate<? super C> filter) {
        return enums(code).filter(filter).filter(c -> matches(c, value)).findAny().orElse(defaultCode);
    }

    public static <C extends CodeEnum<C>> List<C> values(Class<C> code) {
        return values(code, Filters.ANY, ORDINAL_COMPARATOR);
    }

    public static <C extends CodeEnum<C>> List<C> values(Class<C> code, Predicate<? super C> filter) {
        return values(code, filter, ORDINAL_COMPARATOR);
    }

    public static <C extends CodeEnum<C>> List<C> values(Class<C> code, Comparator<? super C> sorter) {
        return values(code, Filters.ANY, sorter);
    }

    public static <C extends CodeEnum<C>> List<C> values(Class<C> code, Predicate<? super C> filter, Comparator<? super C> sorter) {
        return enums(code).filter(filter).sorted(sorter).collect(Collectors.toList());
    }

    public static <C extends CodeEnum<C>> boolean contains(Class<C> code, String value) {
        return contains(code, value, Filters.ANY);
    }

    public static <C extends CodeEnum<C>> boolean contains(Class<C> code, String value, Predicate<? super C> filter) {
        return enums(code).filter(filter).anyMatch(c -> matches(c, value));
    }

    public static <C extends CodeEnum<C>> String shortLabel(Class<C> code, String value, String name) {
        return CodeEnumReflectionUtil.getAnnotatedStringValue(Code.of(code, value), ShortLabel.class, name);
    }

    public static <C extends CodeEnum<C>> String optionalLabel(Class<C> code, String value, String name) {
        return CodeEnumReflectionUtil.getAnnotatedStringValue(Code.of(code, value), OptionalLabel.class, name);
    }
}
