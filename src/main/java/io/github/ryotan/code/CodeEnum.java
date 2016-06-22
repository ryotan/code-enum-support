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

/**
 * コード値を表すインターフェースです。このインターフェースを実装するクラスは、以下のルールを守ってください。
 * <ul>
 * <li>enumとして宣言されること</li>
 * <li>型パラメータ{@code <C>}に自分自身の型を設定すること。</li>
 * </ul>
 * <p>実装クラスの例は次のようになります。</p>
 * <pre><code class="java">
 * public enum Gender implements CodeEnum&lt;Gender&gt; {
 *     NOT_APPLICABLE("9", "適用不能", "適用不能", "not applicable"),
 *     FEMALE("2", "女性", "女", "female"),
 *     MALE("1", "男性", "男", "male"),
 *     NOT_KNOWN("0", "不明", "不明", "not known");
 *
 *     private final String value;
 *     private final String label;
 *     private final String shortLabel;
 *
 *     &#64;OptionalLabel
 *     public final String ENGLISH_LABEL;
 *
 *     &#64;Filter
 *     public static final Predicate&lt;Gender&gt; ISO_5218 = Filters.include(MALE, FEMALE);
 *
 *     Gender(String value, String label, String shortLabel, String english) {
 *         this.value = value;
 *         this.label = label;
 *         this.shortLabel = shortLabel;
 *         this.ENGLISH_LABEL = english;
 *     }
 *
 *     &#64;Override
 *     public String value() {
 *         return this.value;
 *     }
 *
 *     &#64;Override
 *     public String label() {
 *         return this.label;
 *     }
 *
 *     &#64;ShortLabel
 *     public String mark() {
 *         return this.shortLabel;
 *     }
 * }
 * </code></pre>
 *
 * @param <C> 実装クラス自身の型
 */
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
