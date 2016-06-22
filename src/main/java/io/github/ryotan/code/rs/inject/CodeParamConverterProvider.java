/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code.rs.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import io.github.ryotan.code.util.CodeEnumReflectionUtil;

@Provider
public class CodeParamConverterProvider implements ParamConverterProvider {

    private static final ConcurrentHashMap<Class<?>, CodeParamConverter> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (CodeEnumReflectionUtil.isValidCodeEnumClass(rawType)) {
            return (ParamConverter<T>) cache.computeIfAbsent(rawType, (r) -> new CodeParamConverter((Class) rawType));
        }
        return null;
    }
}
