/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.github.ryotan.code.CodeEnum;
import io.github.ryotan.code.CodeEnum.Filter;
import io.github.ryotan.code.CodeEnum.ShortLabel;


/**
 * {@link CodeEnum}のリフレクション関連のユーティリティクラスです。
 * <p>
 * パラメータとして与えられたクラスが{@link CodeEnum}型のクラスである場合、コードパターンや短縮論理名を取得します。
 * </p>
 * <p>
 * {@link Class}を引数に取るものは、与えられた{@link Class}がCodeEnum型でない場合には、{@link IllegalArgumentException}を送出します。
 * </p>
 *
 * @author ryotan
 */
public final class CodeEnumReflectionUtil {

    /**
     * hidden constructor for utility class.
     */
    private CodeEnumReflectionUtil() {
    }

    /**
     * {@link Enum}かつ{@link CodeEnum}のタイプパラメータが{@code aClass}と一致する場合{@code true}を返却します。
     *
     * @param aClass 判定対象のクラス
     * @return タイプパラメータが{@code aClass}と一致する場合{@code true}、{@code aClass}が{@link Enum}または{@link CodeEnum}を継承していない場合{@code false}
     */
    public static boolean isValidCodeEnumClass(Class<?> aClass) {
        if (!Enum.class.isAssignableFrom(aClass) || !CodeEnum.class.isAssignableFrom(aClass)) {
            return false;
        }

        return Stream.of(aClass.getGenericInterfaces()).filter(ParameterizedType.class::isInstance).map(ParameterizedType.class::cast)
                .filter(CodeEnumReflectionUtil::isCodeEnumType).anyMatch(type -> hasSameParameterizedType(type, aClass));
    }

    /**
     * {@link CodeEnum}の実装クラスを返却します。
     * <p>
     * {@code code}がEnum型でない場合は、{@link IllegalArgumentException}を送出します。
     * </p>
     *
     * @param code コード値を表すクラス
     * @param <C>  {@link CodeEnum}の実装クラス
     * @return {@code CodeEnum}の実装クラス
     * @throws IllegalArgumentException {@code code}がEnum型でない場合
     */
    @SuppressWarnings("unchecked")
    public static <C extends Enum<C> & CodeEnum<C>> Class<C> getCodeEnumClass(Class<?> code) {
        if (!isValidCodeEnumClass(code)) {
            throw new IllegalArgumentException(String.format("%s is not a valid CodeEnum class. "
                    + "CodeEnum class must be enum and implement CodeEnum<SELF_TYPE>.", code.getName()));
        }
        return (Class<C>) code;
    }

    /**
     * {@code code}クラスの{@link CodeEnum}で、フィールドやメソッドに指定されたフィルター名が存在した場合、そのオブジェクトを返却します。
     * <p>
     * {@code filter}が{@link CodeEnum}のクラスに存在しない場合、{@link IllegalArgumentException}を送出します。
     * </p>
     *
     * @param code   対象の{@link CodeEnum}のクラス
     * @param filter 対象の{@link CodeEnum}をフィルタリングするフィルター名
     * @param <C>    {@link CodeEnum}の実装クラス
     * @return 指定されたフィルター名のコードパターン
     * @throws IllegalArgumentException {@code filter}が{@link CodeEnum}のクラスに存在しない場合
     */
    public static <C extends Enum<C> & CodeEnum<C>> Predicate<C> getCodeFilter(Class<C> code, String filter) {
        return findCodePatternsFromField(code, filter).orElseGet(() -> findCodePatternsFromMethod(code, filter)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Code filter '%s' for %s is not found.", filter, code))));
    }

    /**
     * {@code code}クラスで{@link Filter}アノテーションが付与されたフィールドのうち、{@code name}と一致するフィールドを返却します。
     *
     * @param code 対象の{@link CodeEnum}のクラス
     * @param name 取得した{@link CodeEnum}がもつフィールド名
     * @param <C>  {@link CodeEnum}の実装クラス
     * @return フィールドオブジェクト {@link Predicate}
     */
    @SuppressWarnings("unchecked")
    private static <C extends Enum<C> & CodeEnum<C>> Optional<Predicate<C>> findCodePatternsFromField(Class<C> code, String name) {
        try {
            Field field = code.getField(name);
            if (isTarget(field, Filter.class, Predicate.class) && isValidCodeFilter(field.getGenericType(), code)) {
                return Optional.ofNullable((Predicate<C>) field.get(code));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // nop
        }
        return Optional.empty();
    }

    /**
     * {@code code}クラスで{@link Filter}アノテーションが付与されたメソッドのうち、{@code name}と一致するメソッド名を返却します。
     *
     * @param code 対象の{@link CodeEnum}のクラス
     * @param name 取得した{@link CodeEnum}がもつメソッド名
     * @param <C>  {@link CodeEnum}の実装クラス
     * @return コードパターン
     */
    @SuppressWarnings("unchecked")
    private static <C extends Enum<C> & CodeEnum<C>> Optional<Predicate<C>> findCodePatternsFromMethod(Class<C> code, String name) {
        try {
            Method method = code.getMethod(name);
            if (isTarget(method, Filter.class, Predicate.class) && isValidCodeFilter(method.getGenericReturnType(), code)) {
                return Optional.ofNullable((Predicate<C>) method.invoke(code));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // nop
        }
        return Optional.empty();
    }

    /**
     * {@code type}が{@link ParameterizedType}のインスタンスで、型名が{@code code}と一致する場合{@code true}を返却します。
     *
     * @param type 対象の型
     * @param code 対象のクラス
     * @return {@code type}の型名が{@code code}と一致する場合{@code true}
     */
    private static boolean isValidCodeFilter(Type type, Class<?> code) {
        if (type instanceof ParameterizedType) {
            final Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            return actualTypeArguments.length == 1 && actualTypeArguments[0].getTypeName().equals(code.getTypeName());
        }
        return false;
    }

    /**
     * {@code C}のコードのうち、{@code name}と一致する短縮論理名を取得します。
     *
     * @param code 対象のコードのクラス
     * @param name 短縮論理名
     * @param <C>  {@link CodeEnum}の実装クラス
     * @return 短縮論理名が示す値
     * @throws IllegalArgumentException {@code C}に短縮論理名が付与されている場合
     */
    public static <C extends Enum<C> & CodeEnum<C>> String getShortLabelValue(C code, String name) {
        try {
            final Field field = code.getClass().getField(name);
            final ShortLabel[] shortLabel = field.getAnnotationsByType(ShortLabel.class);
            if (shortLabel.length != 0) {
                return shortLabel[0].value();
            }
        } catch (NoSuchFieldException e) {
            // nop
        }
        throw new IllegalArgumentException(String.format("%s.%s is not annotated with '@%s'.",
                code.getClass().getSimpleName(), code, ShortLabel.class.getSimpleName()));
    }

    /**
     * フィールド{@code field}にアノテーション{@code maker}が付与されているかつ、{@code field}の型が{@code expectedClass}の型と一致している場合{@code true}返却します。
     *
     * @param field         対象のフィールド
     * @param marker        対象のアノテーション
     * @param expectedClass 比較対象のクラス
     * @return {@code maker}が[{@code field}に付与されていれば{@code true}
     */
    private static boolean isTarget(Field field, Class<? extends Annotation> marker, Class<?> expectedClass) {
        return isAnnotated(field, marker) && expectedClass.isAssignableFrom(field.getType());
    }

    /**
     * メソッド{@code method}にアノテーション{@code maker}が付与されているかつ、{@code method}の型が{@code expectedClass}の型と一致している場合{@code true}返却します。
     *
     * @param method        対象のフィールド
     * @param marker        対象のアノテーション
     * @param expectedClass 比較対象のクラス
     * @return {@code maker}が[{@code method}に付与されていれば{@code true}
     */
    private static boolean isTarget(Method method, Class<? extends Annotation> marker, Class<?> expectedClass) {
        return isAnnotated(method, marker) && expectedClass.isAssignableFrom(method.getReturnType());
    }

    /**
     * アノテーション{@code maker}が{@code annotated}要素に付与されている場合{@code true}を返却します。
     *
     * @param annotated 対象の要素
     * @param marker    対象のアノテーション
     * @return {@code annotated}に{@code maker}が付与されている場合{@code true}
     */
    private static boolean isAnnotated(AnnotatedElement annotated, Class<? extends Annotation> marker) {
        return annotated.isAnnotationPresent(marker);
    }

    /**
     * {@code gif}が{@link CodeEnum}型の場合{@code true}を返却します。
     *
     * @param gif 比較対象の型
     * @return {@code gif}が{@link CodeEnum}型の場合{@code true}
     */
    private static boolean isCodeEnumType(ParameterizedType gif) {
        return gif.getRawType().getTypeName().equals(CodeEnum.class.getName());
    }

    /**
     * {@code aClass}の型が{@code gif}の型と一致する場合{@code true}を返却します。
     *
     * @param gif    対象の型
     * @param aClass 比較対象のクラス
     * @return {@code aClass}の型が{@code gif}の型と一致する場合{@code true}
     */
    private static boolean hasSameParameterizedType(ParameterizedType gif, Class<?> aClass) {
        final Type[] actualTypeArguments = gif.getActualTypeArguments();
        return actualTypeArguments.length == 1 && actualTypeArguments[0].getTypeName().equals(aClass.getName());
    }
}
