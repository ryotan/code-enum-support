/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code.rs.inject;

import javax.ws.rs.ext.ParamConverter;

import io.github.ryotan.code.Code;
import io.github.ryotan.code.CodeEnum;

/**
 * {@link CodeEnum}と{@link String}の間の変換を行う{@link ParamConverter}です。
 * <p>
 * 変換先の{@link CodeEnum}の型パラメータ{@code <C>}には、次の例のように自分自身のクラスを設定する必要があります。
 * </p>
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
 *     &#64;AliasLabel
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
 * @param <C> 変換先の{@link CodeEnum}の型。{@link Enum}である必要があります。
 */
public class CodeParamConverter<C extends CodeEnum<C>> implements ParamConverter<C> {

    /**
     * 変換先の{@link CodeEnum}の{@link Class}。
     */
    private final Class<C> code;

    public CodeParamConverter(Class<C> code) {
        this.code = code;
    }

    @Override
    public C fromString(String value) {
        return Code.or(this.code, value, null);
    }

    @Override
    public String toString(C value) {
        return value.value();
    }
}
