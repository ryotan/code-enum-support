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
    NOT_APPLICABLE("9", "適用不能", "適用不能", "not applicable"),
    FEMALE("2", "女性", "女", "female"),
    MALE("1", "男性", "男", "male"),
    NOT_KNOWN("0", "不明", "不明", "not known");

    private final String value;
    private final String label;
    private final String shortLabel;

    @OptionalLabel
    public final String ENGLISH_LABEL;

    @Filter
    public static final Predicate<Gender> ISO_5218 = Filters.include(MALE, FEMALE);

    Gender(String value, String label, String shortLabel, String english) {
        this.value = value;
        this.label = label;
        this.shortLabel = shortLabel;
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

    @ShortLabel
    public String mark() {
        return this.shortLabel;
    }
}
