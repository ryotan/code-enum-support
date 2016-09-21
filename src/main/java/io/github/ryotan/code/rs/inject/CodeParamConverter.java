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
