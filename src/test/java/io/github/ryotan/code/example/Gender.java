/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code.example;

import java.util.function.Predicate;

import io.github.ryotan.code.CodeEnum;

public enum Gender implements CodeEnum<Gender> {
    @AliasLabel(name = "english", label = "not applicable")
    NOT_APPLICABLE("9", "適用不能"),

    @ShortLabel("女")
    @AliasLabel(name = "english", label = "female")
    FEMALE("2", "女性"),

    @ShortLabel("男")
    @AliasLabel(name = "english", label = "male")
    MALE("1", "男性"),

    @AliasLabel(name = "english", label = "not known")
    NOT_KNOWN("0", "不明");

    private final String value;
    private final String label;

    @Filter
    public static final Predicate<Gender> JIS_X0303 = Filters.include(MALE, FEMALE);

    Gender(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String label() {
        return this.label;
    }
}
