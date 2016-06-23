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

public final class CodeEnumReflectionUtil {
    private CodeEnumReflectionUtil() {
    }

    public static boolean isValidCodeEnumClass(Class<?> aClass) {
        if (!Enum.class.isAssignableFrom(aClass) || !CodeEnum.class.isAssignableFrom(aClass)) {
            return false;
        }

        return Stream.of(aClass.getGenericInterfaces()).filter(ParameterizedType.class::isInstance).map(ParameterizedType.class::cast)
                .filter(CodeEnumReflectionUtil::isCodeEnumType).anyMatch(type -> hasSameParameterizedType(type, aClass));
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends CodeEnum> getCodeEnumClass(Class<?> code) {
        if (!isValidCodeEnumClass(code)) {
            throw new IllegalArgumentException(String.format("%s is not a valid CodeEnum class. "
                    + "CodeEnum class must be enum and implement CodeEnum<SELF_TYPE>.", code.getName()));
        }
        return (Class<? extends CodeEnum>) code;
    }

    public static <C extends CodeEnum<C>> Predicate<C> getCodeFilter(Class<C> code, String filter) {
        return findCodePatternsFromField(code, filter).orElseGet(() -> findCodePatternsFromMethod(code, filter)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Code filter '%s' for %s is not found.", filter, code))));
    }

    @SuppressWarnings("unchecked")
    private static <C extends CodeEnum<C>> Optional<Predicate<C>> findCodePatternsFromField(Class<C> code, String name) {
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

    @SuppressWarnings("unchecked")
    private static <C extends CodeEnum<C>> Optional<Predicate<C>> findCodePatternsFromMethod(Class<C> code, String name) {
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

    private static boolean isValidCodeFilter(Type type, Class<?> code) {
        if (type instanceof ParameterizedType) {
            final Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            return actualTypeArguments.length == 1 && actualTypeArguments[0].getTypeName().equals(code.getTypeName());
        }
        return false;
    }

    public static <C extends CodeEnum<C>> String getAnnotatedStringValue(C code, Class<? extends Annotation> marker, String name) {
        return findAnnotatedStringValueFromField(code, marker, name)
                .orElseGet(() -> findAnnotatedStringValueFromMethod(code, marker, name)
                        .orElseThrow(() -> new IllegalArgumentException(
                                String.format("The field or method annotated as '%s' with name '%s' is not found in %s", marker.getName(), name, code)
                        )));
    }

    private static <C extends CodeEnum<C>> Optional<String> findAnnotatedStringValueFromField(C code, Class<? extends Annotation> marker, String name) {
        try {
            Field field = code.getClass().getField(name);
            if (isTarget(field, marker, String.class)) {
                return Optional.ofNullable((String) field.get(code));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // nop
        }
        return Optional.empty();
    }

    private static <C extends CodeEnum<C>> Optional<String> findAnnotatedStringValueFromMethod(C code, Class<? extends Annotation> marker, String name) {
        try {
            Method method = code.getClass().getMethod(name);
            if (isTarget(method, marker, String.class)) {
                return Optional.ofNullable((String) method.invoke(code));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // nop
        }
        return Optional.empty();
    }

    private static boolean isTarget(Field field, Class<? extends Annotation> marker, Class<?> expectedClass) {
        return isAnnotated(field, marker) && expectedClass.isAssignableFrom(field.getType());
    }

    private static boolean isTarget(Method method, Class<? extends Annotation> marker, Class<?> expectedClass) {
        return isAnnotated(method, marker) && expectedClass.isAssignableFrom(method.getReturnType());
    }

    private static boolean isAnnotated(AnnotatedElement annotated, Class<? extends Annotation> marker) {
        return annotated.isAnnotationPresent(marker);
    }

    private static boolean isCodeEnumType(ParameterizedType gif) {
        return gif.getRawType().getTypeName().equals(CodeEnum.class.getName());
    }

    private static boolean hasSameParameterizedType(ParameterizedType gif, Class<?> aClass) {
        final Type[] actualTypeArguments = gif.getActualTypeArguments();
        return actualTypeArguments.length == 1 && actualTypeArguments[0].getTypeName().equals(aClass.getName());
    }
}
