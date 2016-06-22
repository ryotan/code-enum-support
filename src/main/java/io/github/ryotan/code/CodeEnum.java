/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface CodeEnum<C extends CodeEnum<C>> {
    String value();

    String label();

    int ordinal();

    @SuppressWarnings("unchecked")
    default boolean in(C... codes) {
        return Stream.of(codes).anyMatch(this::equals);
    }

    @SuppressWarnings("unchecked")
    default boolean not(C... codes) {
        return !this.in(codes);
    }

    final class Filters {
        private Filters() {
        }

        public static Predicate<? super CodeEnum<?>> ANY = c -> true;

        @SafeVarargs
        public static <T extends CodeEnum<T>> Predicate<T> include(T... includes) {
            return c -> Stream.of(includes).anyMatch(c::equals);
        }

        @SafeVarargs
        public static <T extends CodeEnum<T>> Predicate<T> exclude(T... excludes) {
            return include(excludes).negate();
        }
    }

    @Target({METHOD, FIELD})
    @Retention(RUNTIME)
    @Documented
    @interface Filter {
    }

    @Target({METHOD, FIELD})
    @Retention(RUNTIME)
    @Documented
    @interface ShortLabel {
    }

    @Target({METHOD, FIELD})
    @Retention(RUNTIME)
    @Documented
    @interface OptionalLabel {
    }
}
