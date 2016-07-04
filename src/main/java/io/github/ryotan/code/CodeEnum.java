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
 * コード値を表すインターフェースです。
 *
 * <h3>実装に関する規約</h3>
 * <p>
 * このインターフェースを実装するクラスは、以下の規約に従ってください。
 * </p>
 * <ul>
 * <li>enumとして宣言されること</li>
 * <li>型パラメータ{@code <C>}に自分自身の型を設定すること。</li>
 * </ul>
 * <p>実装クラスの例は次のようになります。</p>
 * <pre><code class="java">
 * public enum Gender implements CodeEnum&lt;Gender&gt; {
 *     NOT_APPLICABLE("9", "適用不能"),
 *     FEMALE("2", "女性"),
 *     MALE("1", "男性"),
 *     NOT_KNOWN("0", "不明");
 *
 *     private final String value;
 *     private final String label;
 *
 *     Gender(String value, String label, String shortLabel, String english) {
 *         this.value = value;
 *         this.label = label;
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
 * }
 * </code></pre>
 *
 * <h3>コードのフィルタリング</h3>
 * <p>
 * コードによっては、精査などで使うときに一部の値に絞り込みたい場合もあります。
 * そういった場合は、{@link Filter}アノテーションを付けたフィールドもしくはメソッドを作成してください。
 * これらからは、{@link Predicate Predicate&lt;C&gt;}を取得できる必要があります。
 * </p>
 * <p>
 * たとえば、上で例にあげた{@code Gender}を（とても古いシステムと連携しなくてはいけないなどの理由で）JIS
 * X0303に従ってフィルタリングする必要がある場合には、次のようなフィールドを定義して利用することができます。
 * </p>
 * <pre><code class="java">
 * &#64;Filter
 * public static final Predicate&lt;Gender&gt; JIS_X0303 = Filters.include(MALE, FEMALE);
 * </code></pre>
 * <p>
 * 定義した{@code JIS_X0303}を利用して、JIS X0303に含まれるコード値のみを取得することが出来ます。
 * </p>
 * <pre><code class="java">
 * Code.values(Gender.class, Gender.JIS_X0303)
 * </code></pre>
 *
 * <h3>コードの別名</h3>
 * <p>
 * コードによっては論理名を{@link #label()}以外に設定したい場合もあります。そういった場合は、{@link AliasLabel}アノテーションを利用してください。
 * {@link AliasLabel}アノテーションはフィールドもしくはメソッドに対して付けることができます。ただし、これらから{@link String}が取得できない
 * （フィールドの場合は{@link String}型で定義されていない。メソッドの場合は戻り値の方が{@link String}でない）場合は単純に無視されます。
 * </p>
 * <p>
 * 例えば、{@code Gender}に英語の別名を付けたい場合、次のようにして定義することが出来ます。
 * </p>
 * <pre><code class="java">
 * &#64;AliasLabel
 * public final String ENGLISH_LABEL;
 * </code></pre>
 * <p>
 * 定義されている別名は、{@link Code#alias(Class, String, String)}で取得することが出来ます。このとき、{@code name}パラメータには、
 * {@link AliasLabel}アノテーションを付与したフィールドあるいはメソッドの名称を設定してください。
 * </p>
 *
 *
 * @param <C> 実装クラス自身の型
 * @see Code
 * @see Filters
 */
public interface CodeEnum<C extends CodeEnum<C>> {
    /**
     * Enumが表すコードのコード値を返します。
     *
     * @return コード値
     */
    String value();

    /**
     * Enumが表すコードの論理名を返します。
     *
     * @return コードの論理名
     */
    String label();

    /**
     * 規約に従った実装クラスでは、{@link Enum#ordinal()}が実装されるので独自に実装しないでください。
     *
     * @return {@link Enum#ordinal()}
     */
    int ordinal();

    /**
     * コードが{@code codes}で与えられたコードに含まれているかどうかを返します。
     *
     * @param codes 判定対象のコードの集合
     * @return コードが {@code codes} に含まれる場合 {@code true}
     */
    @SuppressWarnings("unchecked")
    default boolean in(C... codes) {
        return Stream.of(codes).anyMatch(this::equals);
    }

    /**
     * コードが{@code codes}で与えられたコードに含まれないかどうかを返します。
     *
     * @param codes 判定対象のコードの集合
     * @return コードが {@code codes} に含まれない場合 {@code true}
     */
    @SuppressWarnings("unchecked")
    default boolean not(C... codes) {
        return !this.in(codes);
    }

    /**
     * {@link CodeEnum}のフィルタリングをする{@link Predicate}を楽に生成できるようにするユーティリティクラスです。
     */
    final class Filters {

        /**
         * 任意の{@link CodeEnum}に対して{@code true}を返します。
         */
        public static final Predicate<? super CodeEnum<?>> ANY = c -> true;

        /**
         * hidden constructor for utility class.
         */
        private Filters() {
        }

        /**
         * {@code includes}に与えられたコードに含まれるかどうかを判定する{@link Predicate}を返します。
         *
         * @param includes 判定対象のコードの集合
         * @param <T>      判定対象のコードの型
         * @return {@code includes}に含まれる{@code T}のEnumが渡されたときに{@code true}を返す{@link Predicate}
         */
        @SafeVarargs
        public static <T extends CodeEnum<T>> Predicate<T> include(T... includes) {
            return c -> Stream.of(includes).anyMatch(c::equals);
        }

        /**
         * {@code excludes}に与えられたコードに含まれないかどうかを判定する{@link Predicate}を返します。
         *
         * @param excludes 判定対象のコードの集合
         * @param <T>      判定対象のコードの型
         * @return {@code excludes}に含まれない{@code T}のEnumが渡されたときに{@code true}を返す{@link Predicate}
         */
        @SafeVarargs
        public static <T extends CodeEnum<T>> Predicate<T> exclude(T... excludes) {
            return include(excludes).negate();
        }
    }

    /**
     * コードをフィルタリングするための関数を表すマーカーインターフェースです。
     * <p>
     * 次のように定義することが出来ます。
     * </p>
     * <pre><code class="java">
     * </code></pre>
     * <p>
     * 次のように利用することで、フィルタリングされたコードの集合を取得できます。
     * </p>
     * <pre><code class="java">
     * </code></pre>
     * <p>
     * また、フィルタリングされたコードの集合に含まれるかどうかを判定することも出来ます。
     * </p>
     * <pre><code class="java">
     * </code></pre>
     */
    @Target({METHOD, FIELD})
    @Retention(RUNTIME)
    @Documented
    @interface Filter {
    }

    /**
     * コードの論理名の短縮名を表すために利用するアノテーションです。
     * <p>
     * 次のように、Enum定数に対してアノテーションを付けることで、短縮論理名を設定することが出来ます。
     * </p>
     * <pre><code class="java">
     * &#64;ShortLabel("男")
     * MALE("1", "男性"),
     * </code></pre>
     * <p>
     * なお、アノテーションを付けるフィールドは{@link String}型でなくてはいけません。
     * </p>
     *
     * @deprecated 後方互換製のためだけに用意されています。別名が必要な場合は、{@link AliasLabel} を利用することを推奨します。
     */
    @Deprecated
    @Target({FIELD})
    @Retention(RUNTIME)
    @Documented
    @interface ShortLabel {
        /**
         * コードの短縮論理名を返します。
         *
         * @return コードの短縮論理名
         */
        String value();
    }

    /**
     * コードの別名を表すためのマーカーインターフェースです。
     * <p>
     * 次のように、Enumの持つフィールドもしくはメソッドに対してアノテーションを付けることで、別名を設定することが出来ます。
     * なお、アノテーションを付けるフィールドもしくはメソッドは、{@link String}型を返さなくてはいけません。
     * </p>
     * <pre><code class="java">
     * // フィールドに付ける場合。
     * &#64;AliasLabel
     * public final String ENGLISH_LABEL;
     *
     * // メソッドに付ける場合。
     * &#64;AliasLabel
     * public String englishLabel {
     *     return ENGLISH_LABEL;
     * };
     * </code></pre>
     * <p>
     * 設定した別名は、{@link Code#alias(Class, String, String)}で取得することが出来ます。このとき{@code name}には、このアノテーションを付与した
     * フィールドもしくはメソッドの名前を指定してください。上記の例の場合は、次のようになります。
     * </p>
     * <pre><code class="java">
     * // 上の例で、フィールドを参照する場合。
     * Code.aliasLabel(Gender.class, "1", "ENGLISH_LABEL");
     *
     * // 上の例で、メソッドを参照する場合。
     * Code.aliasLabel(Gender.class, "1", "englishLabel");
     * </code></pre>
     *
     * @see Code#alias(Class, String, String)
     */
    @Target({METHOD, FIELD})
    @Retention(RUNTIME)
    @Documented
    @interface AliasLabel {
    }
}
