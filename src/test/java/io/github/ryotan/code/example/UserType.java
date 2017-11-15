/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code.example;

import io.github.ryotan.code.CodeEnum;

public enum UserType implements CodeEnum<UserType> {
    @AliasLabel(name = "english", label = "ADMINISTRATOR")
    ADMINISTRATOR("1", "管理者"),
    @AliasLabel(name = "english", label = "OPERATOR")
    OPERATOR("2", "運用者"),
    @AliasLabel(name = "english", label = "MEMBER")
    MEMBER("3", "利用者");

    private final String value;
    private final String label;

    UserType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String label() {
        return label;
    }
}
