/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code.example;

import io.github.ryotan.code.CodeEnum;

public enum NotEnum implements CodeEnum<NotEnum> {
    ;

    @Override
    public String value() {
        return null;
    }

    @Override
    public String label() {
        return null;
    }
}
