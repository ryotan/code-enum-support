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

import io.github.ryotan.code.CodeEnum.AliasLabel;
import io.github.ryotan.code.CodeEnum.Filters;
import io.github.ryotan.code.CodeEnum.ShortLabel;
import io.github.ryotan.code.util.CodeEnumReflectionUtil;

/**
 * {@link CodeEnum}を扱うためのユーティリティクラスです。
 * <p>
 * {@link CodeEnum}のクラスとコード値から{@link CodeEnum}のEnum定数を生成したり、ある{@link CodeEnum}に含まれるEnum定数の一覧を取得したり出来ます。
 * </p>
 * <p>
 * {@link Class}を引数に取るものは、与えられた{@link Class}がEnum型でない（{@link Class#isEnum()}が{@code false}を返す）場合には、
 * {@link IllegalArgumentException}を送出します。
 * </p>
 *
 * @author ryotan
 */
public final class Code {

    /**
     * {@link Enum#ordinal()}で順序を比較する{@link Comparator}
     */
    private static final Comparator<CodeEnum<?>> ORDINAL_COMPARATOR = (c1, c2) -> c1.ordinal() - c2.ordinal();

    /**
     * hidden constructor for utility class.
     */
    private Code() {
    }

    /**
     * {@code candidate}のコード値が{@code value}と一致する場合は{@code true}を返します。
     *
     * @param candidate 比較対象の{@link CodeEnum}
     * @param value     比較対象のコード値
     * @return {@code candidate}のコード値が{@code value}と一致する場合は{@code true}
     */
    private static boolean matches(CodeEnum<?> candidate, String value) {
        return candidate.value().equals(value);
    }

    /**
     * {@code code}に含まれるEnum定数を{@link Stream}として返却します。
     * <p>
     * {@code code}がEnum型出ない場合は、{@link IllegalArgumentException}を送出します。
     * </p>
     *
     * @param code 対象の{@link CodeEnum}のクラス
     * @param <C>  対象の{@link CodeEnum}の型
     * @return {@code C}に含まれるEnum定数の{@link Stream}
     * @throws IllegalArgumentException {@code code}がEnum型でない場合
     */
    private static <C extends CodeEnum<C>> Stream<C> enums(Class<C> code) {
        if (code.isEnum()) {
            return Stream.of(code.getEnumConstants());
        }
        throw new IllegalArgumentException(String.format("CodeEnum must be enum. class=[%s].", code.getName()));
    }

    /**
     * {@code code}クラスの{@link CodeEnum}で、コード値（{@link CodeEnum#value()}の値）が{@code value}に一致するコードを返却します。
     * <p>
     * 一致するコードが{@code C}の中に見つからなかった場合は、{@link IllegalArgumentException}を送出します。
     * </p>
     *
     * @param code  対象の{@link CodeEnum}のクラス
     * @param value 取得したい{@link CodeEnum}がもつコード値
     * @param <C>   対象の{@link CodeEnum}の型
     * @return {@code value}がコード値であるような{@link CodeEnum}({@link C}のEnum定数)
     * @throws IllegalArgumentException {@code code}にコード値が{@code value}であるコードが存在しない場合
     */
    public static <C extends CodeEnum<C>> C of(Class<C> code, String value) {
        return of(code, value, Filters.ANY);
    }

    /**
     * {@code code}クラスの{@link CodeEnum}のうち、{@code filter}を満たすもので、
     * コード値（{@link CodeEnum#value()}の値）が{@code value}に一致するコードを返却します。
     * <p>
     * 一致するコードが{@code C}の中に見つからなかった場合は、{@link IllegalArgumentException}を送出します。
     * </p>
     *
     * @param code   対象の{@link CodeEnum}のクラス
     * @param value  取得したい{@link CodeEnum}がもつコード値
     * @param filter 対象の{@link CodeEnum}をフィルタリングする{@link Predicate}
     * @param <C>    対象の{@link CodeEnum}の型
     * @return {@code value}がコード値であるような{@link CodeEnum}({@link C}のEnum定数)
     * @throws IllegalArgumentException {@code code}にコード値が{@code value}であるコードが存在しない場合
     */
    public static <C extends CodeEnum<C>> C of(Class<C> code, String value, Predicate<? super C> filter) {
        return enums(code).filter(filter).filter(c -> matches(c, value)).findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Code is not found. code=[%s], value=[%s]", code.getName(), value)));
    }

    /**
     * {@code code}クラスの{@link CodeEnum}で、コード値（{@link CodeEnum#value()}の値）が{@code value}に一致するコードを返却します。
     * <p>
     * 一致するコードが{@code C}の中に見つからなかった場合は、{@code defaultCode}をそのまま返却します。
     * </p>
     *
     * @param code        対象の{@link CodeEnum}のクラス
     * @param value       取得したい{@link CodeEnum}がもつコード値
     * @param defaultCode {@code value}が{@code code}に存在しない場合のデフォルト値
     * @param <C>         対象の{@link CodeEnum}の型
     * @return {@code value}がコード値であるような{@link CodeEnum}({@link C}のEnum定数)。存在しない場合は、{@code defaultCode}
     */
    public static <C extends CodeEnum<C>> C or(Class<C> code, String value, C defaultCode) {
        return or(code, value, defaultCode, Filters.ANY);
    }

    /**
     * {@code code}クラスの{@link CodeEnum}のうち、{@code filter}を満たすもので、
     * コード値（{@link CodeEnum#value()}の値）が{@code value}に一致するコードを返却します。
     * <p>
     * 一致するコードが{@code C}の中に見つからなかった場合は、{@code defaultCode}をそのまま返却します。
     * </p>
     *
     * @param code   対象の{@link CodeEnum}のクラス
     * @param value  取得したい{@link CodeEnum}がもつコード値
     * @param defaultCode {@code value}が{@code code}に存在しない場合のデフォルト値
     * @param filter 対象の{@link CodeEnum}をフィルタリングする{@link Predicate}
     * @param <C>    対象の{@link CodeEnum}の型
     * @return {@code value}がコード値であるような{@link CodeEnum}({@link C}のEnum定数)。存在しない場合は、{@code defaultCode}
     */
    public static <C extends CodeEnum<C>> C or(Class<C> code, String value, C defaultCode, Predicate<? super C> filter) {
        return enums(code).filter(filter).filter(c -> matches(c, value)).findAny().orElse(defaultCode);
    }

    /**
     * {@link C}に含まれるコードのリストを返します。返されるリストは{@link Enum#ordinal()}でソートされています。
     *
     * @param code コードのリストを取得する対象の{@link CodeEnum}
     * @param <C>  コードのリストを取得する対象の型
     * @return {@link C}に含まれるコードのリスト
     */
    public static <C extends CodeEnum<C>> List<C> values(Class<C> code) {
        return values(code, Filters.ANY, ORDINAL_COMPARATOR);
    }

    /**
     * {@link C}に含まれるコードのうち、{@code filter}を満たすコードのリストを返します。返されるリストは{@link Enum#ordinal()}でソートされています。
     *
     * @param code   コードのリストを取得する対象の{@link CodeEnum}
     * @param filter 対象の{@link CodeEnum}をフィルタリングする{@link Predicate}
     * @param <C>    コードのリストを取得する対象の型
     * @return {@link C}に含まれるコードのリスト
     */
    public static <C extends CodeEnum<C>> List<C> values(Class<C> code, Predicate<? super C> filter) {
        return values(code, filter, ORDINAL_COMPARATOR);
    }

    /**
     * {@link C}に含まれるコードのリストを返します。返されるリストは{@code sorter}でソートされています。
     *
     * @param code   コードのリストを取得する対象の{@link CodeEnum}
     * @param sorter コードのリストをソートするのに利用する{@link Comparator}
     * @param <C>    コードのリストを取得する対象の型
     * @return {@link C}に含まれるコードのリスト
     */
    public static <C extends CodeEnum<C>> List<C> values(Class<C> code, Comparator<? super C> sorter) {
        return values(code, Filters.ANY, sorter);
    }

    /**
     * {@link C}に含まれるコードのうち、{@code filter}を満たすコードのリストを返します。返されるリストは{@code sorter}でソートされています。
     *
     * @param code   コードのリストを取得する対象の{@link CodeEnum}
     * @param filter 対象の{@link CodeEnum}をフィルタリングする{@link Predicate}
     * @param sorter コードのリストをソートするのに利用する{@link Comparator}
     * @param <C>    コードのリストを取得する対象の型
     * @return {@link C}に含まれるコードのリスト
     */
    public static <C extends CodeEnum<C>> List<C> values(Class<C> code, Predicate<? super C> filter, Comparator<? super C> sorter) {
        return enums(code).filter(filter).sorted(sorter).collect(Collectors.toList());
    }

    /**
     * {@code C}にコード値が{@code value}であるコードが存在するかどうかを判定します。
     *
     * @param code  対象のコードのクラス
     * @param value 含まれるかどうかを判定するコード値
     * @param <C>   対象のコードを表す型
     * @return {@code C}のコードにコード値が{@code value}であるものが存在する場合{@code true}
     */
    public static <C extends CodeEnum<C>> boolean contains(Class<C> code, String value) {
        return contains(code, value, Filters.ANY);
    }

    /**
     * {@code C}のコードのうち、{@code filter}を満たすものでコード値が{@code value}であるコードが存在するかどうかを判定します。
     *
     * @param code   対象のコードのクラス
     * @param value  含まれるかどうかを判定するコード値
     * @param filter 対象の{@link CodeEnum}をフィルタリングする{@link Predicate}
     * @param <C>    対象のコードを表す型
     * @return {@code C}のコードに{@code filter}を満たし、コード値が{@code value}であるものが存在する場合{@code true}
     */
    public static <C extends CodeEnum<C>> boolean contains(Class<C> code, String value, Predicate<? super C> filter) {
        return enums(code).filter(filter).anyMatch(c -> matches(c, value));
    }

    /**
     * {@code C}のコードのうち、コード値が{@code value}であるコードの短縮論理名を取得します。
     * （とあるFWのためだけに用意されています。別名が必要な場合は、{@link AliasLabel} を利用したほうが統一感があるので良いと思います。）
     *
     * @param code  対象のコードのクラス
     * @param value 短縮論理名を取得したいコードのコード値
     * @param <C>   対象のコードを表す型
     * @return コード値が{@code value}であるコードの短縮論理名
     * @throws IllegalArgumentException コードに{@link ShortLabel}が付けられていない、
     *                                  もしくは{@code code}にコード値が{@code value}であるコードが存在しない場合
     * @see ShortLabel
     * @see AliasLabel
     */
    public static <C extends CodeEnum<C>> String shortLabel(Class<C> code, String value) {
        C target = Code.of(code, value);
        return CodeEnumReflectionUtil.getShortLabelValue(target, ((Enum<?>) target).name());
    }

    /**
     * {@code C}のコードのうち、コード値が{@code value}であるコードの別名を取得します。
     * <p>
     * 別名は、対象のコードの{@link AliasLabel}が付けられていて名前が{@code aliasName}と完全一致するフィールドもしくはメソッドの値として取得します。
     * </p>
     *
     * @param code      対象のコードのクラス
     * @param value     短縮論理名を取得したいコードのコード値
     * @param aliasName {@link AliasLabel}の付けられているフィールド名もしくはメソッド名
     * @param <C>       対象のコードを表す型
     * @return コード値が{@code value}であるコードの短縮論理名
     * @throws IllegalArgumentException {@link AliasLabel}が付けられていて名前が{@code aliasName}と完全一致するフィールドもしくはメソッドがない、
     *                                  もしくは{@code code}にコード値が{@code value}であるコードが存在しない場合
     * @see AliasLabel
     */
    public static <C extends CodeEnum<C>> String alias(Class<C> code, String value, String aliasName) {
        return CodeEnumReflectionUtil.getAnnotatedStringValue(Code.of(code, value), AliasLabel.class, aliasName);
    }
}
