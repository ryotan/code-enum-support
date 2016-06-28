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

/**
 * コード値に対する操作を行うクラス
 *
 */
public final class Code {

    private static final Comparator<CodeEnum<?>> ORDINAL_COMPARATOR = (c1, c2) -> c1.ordinal() - c2.ordinal();
    
    /**
     * コンストラクタ
     */
    private Code() {
    }

   /**
    * コード値が特定の値と一致するか否かを判定する。
    * 
    * @param candidate コード値
    * @param value 判定対象の値
    * @return 一致する場合true、一致しない場合false
    */
    private static boolean matches(CodeEnum<?> candidate, String value) {
        return candidate.value().equals(value);
    }

    /**
     * コードが示すすべての値を取得する。
     * 
     * @param code コード
     * @return コード値のストリート
     */
    private static <C extends CodeEnum<C>> Stream<C> enums(Class<C> code) {
        if (code.isEnum()) {
            return Stream.of(code.getEnumConstants());
        }
        throw new IllegalArgumentException(String.format("CodeEnum must be enum. class=[%s].", code.getName()));
    }

    /**
     * コード値のうちすべての範囲の中で特定の値と一致するコード値を取得する。
     * 
     * @param code コード値
     * @param value 判定対象の値
     * @return コード値のストリート
     */
    public static <C extends CodeEnum<C>> C of(Class<C> code, String value) {
        return of(code, value, Filters.ANY);
    }

    /**
     * コード値のうち指定された範囲の中で特定の値と一致するコード値を取得する。
     * 
     * @param code コード値
     * @param value 判定対象の値
     * @param filter 指定範囲
     * @return コード値のストリート
     */
    public static <C extends CodeEnum<C>> C of(Class<C> code, String value, Predicate<? super C> filter) {
        return enums(code).filter(filter).filter(c -> matches(c, value)).findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Code is not found. code=[%s], value=[%s]", code.getName(), value)));
    }

    /**
     * コード値のうちすべての範囲の中で特定の値と一致するコード値を取得する。
     * 一致しない場合デフォルトのコード値を返却する。
     * 
     * @param code コード値
     * @param value 判定対象の値
     * @param defaultCode デフォルトのコード値
     * @return 特定の値と一致するコード値もしくはデフォルトのコード値
     */
    public static <C extends CodeEnum<C>> C or(Class<C> code, String value, C defaultCode) {
        return or(code, value, defaultCode, Filters.ANY);
    }

    /**
     *  コード値のうち指定された範囲の中で特定の値と一致するコード値を取得する。
     *  一致しない場合デフォルトのコード値を返却する。
     * 
     * @param code コード値
     * @param value 判定対象の値
     * @param defaultCode デフォルトのコード値
     * @param filter 指定範囲
     * @return 特定の値と一致するコード値もしくはデフォルトのコード値
     */
    public static <C extends CodeEnum<C>> C or(Class<C> code, String value, C defaultCode, Predicate<? super C> filter) {
        return enums(code).filter(filter).filter(c -> matches(c, value)).findAny().orElse(defaultCode);
    }

    /**
     * コード値のうちすべての範囲内にある値を昇順にソートしたリストを取得する。
     * 
     * @param code
     * @return
     */
    public static <C extends CodeEnum<C>> List<C> values(Class<C> code) {
        return values(code, Filters.ANY, ORDINAL_COMPARATOR);
    }

    /**
     * コード値のうち指定された範囲内にある値を昇順にソートしたリストを取得する。
     * 
     * @param code コード値
     * @param filter 指定範囲
     * @return コード値のリスト
     */
    public static <C extends CodeEnum<C>> List<C> values(Class<C> code, Predicate<? super C> filter) {
        return values(code, filter, ORDINAL_COMPARATOR);
    }

    /**
     * コード値のうちすべての範囲内にある値をソートしたリストを取得する。
     * 
     * @param code コード値
     * @param sorter ソート条件
     * @return コード値のリスト
     */
   public static <C extends CodeEnum<C>> List<C> values(Class<C> code, Comparator<? super C> sorter) {
        return values(code, Filters.ANY, sorter);
    }

    /**
     * コード値のうち指定された範囲内にある値をソートしたリストを取得する。
     * 
     * @param code コード値
     * @param filter 指定範囲
     * @param sorter ソート条件
     * @return コード値のリスト
     */
    public static <C extends CodeEnum<C>> List<C> values(Class<C> code, Predicate<? super C> filter, Comparator<? super C> sorter) {
        return enums(code).filter(filter).sorted(sorter).collect(Collectors.toList());
    }

    /**
     * コード値の中にすべての範囲にある特定の値が存在するか否かを判定する。
     * 
     * @param code コード値
     * @param value 判定対象の値
     * @return 存在する場合true、存在しない場合false
     */
    public static <C extends CodeEnum<C>> boolean contains(Class<C> code, String value) {
        return contains(code, value, Filters.ANY);
    }

    /**
     * コード値の中に指定された範囲にある特定の値が存在するか否かを判定する。
     * 
     * @param code コード値
     * @param value 判定対象の値
     * @param filter 指定範囲
     * @return 存在する場合true、存在しない場合false
     */
    public static <C extends CodeEnum<C>> boolean contains(Class<C> code, String value, Predicate<? super C> filter) {
        return enums(code).filter(filter).anyMatch(c -> matches(c, value));
    }

    /**
     * コード値の特定の値のアノテーション（ショートラベル）を取得する。
     * 
     * @param code コード値
     * @param value 判定対象の値
     * @param name アノテーション取得対象のフィールド名もしくはメソッド名
     * @return ショートラベルの値
     */
    public static <C extends CodeEnum<C>> String shortLabel(Class<C> code, String value, String name) {
        return CodeEnumReflectionUtil.getAnnotatedStringValue(Code.of(code, value), ShortLabel.class, name);
    }

    /**
     * コード値の特定の値のアノテーション（オプションラベル）を取得する。
     * 
     * @param code コード値
     * @param value 判定対象の値
     * @param name アノテーション取得対象のフィールド名もしくはメソッド名
     * @return オプションラベル
     */
    public static <C extends CodeEnum<C>> String optionalLabel(Class<C> code, String value, String name) {
        return CodeEnumReflectionUtil.getAnnotatedStringValue(Code.of(code, value), OptionalLabel.class, name);
    }
}
