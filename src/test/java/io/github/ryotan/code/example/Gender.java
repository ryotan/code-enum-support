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
    @ShortLabel("適用不能")
    NOT_APPLICABLE("9", "適用不能", "not applicable"),

    @ShortLabel("女")
    FEMALE("2", "女性", "female"),

    @ShortLabel("男")
    MALE("1", "男性", "male"),

    @ShortLabel("不明")
    NOT_KNOWN("0", "不明", "not known");

    private final String value;
    private final String label;

    @AliasLabel
    public final String ENGLISH_LABEL;

    @Filter
    public static final Predicate<Gender> JIS_X0303 = Filters.include(MALE, FEMALE);

    Gender(String value, String label, String english) {
        this.value = value;
        this.label = label;
        this.ENGLISH_LABEL = english;
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
