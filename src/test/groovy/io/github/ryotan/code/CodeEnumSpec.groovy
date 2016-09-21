/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code

import io.github.ryotan.code.example.Gender
import io.github.ryotan.code.example.UserType
import spock.lang.Specification

/**
 * Specification class of {@link CodeEnum}
 */
class CodeEnumSpec extends Specification {
  def "CodeEnum#inは、自分自身が引数に含まれる場合のみtrueを返すこと。"() {
    expect:
    code.in(Gender.NOT_APPLICABLE, Gender.FEMALE) == included
    code.not(Gender.NOT_APPLICABLE, Gender.FEMALE) == excluded

    where:
    code                  | included | excluded
    Gender.MALE           | false    | true
    Gender.NOT_APPLICABLE | true     | false
    Gender.FEMALE         | true     | false
    Gender.NOT_KNOWN      | false    | true
  }

  def "CodeEnum.Filters#includeは、引数のCodeEnumに含まれる値が渡されたときだけtrueを返すPredicateを返すこと。"() {
    expect:
    CodeEnum.Filters.include(Gender.MALE, Gender.NOT_KNOWN).test(code) == included
    CodeEnum.Filters.exclude(Gender.MALE, Gender.NOT_KNOWN).test(code) == excluded

    where:
    code                  | included | excluded
    Gender.MALE           | true     | false
    Gender.NOT_APPLICABLE | false    | true
    Gender.FEMALE         | false    | true
    Gender.NOT_KNOWN      | true     | false
  }

  def "CodeEnum.Filters.ANYは、任意のCodeEnumに対してtrueを返すPredicateを返すこと。"() {
    expect:
    CodeEnum.Filters.ANY.test(code)

    where:
    code                   | _
    Gender.MALE            | _
    Gender.NOT_APPLICABLE  | _
    Gender.FEMALE          | _
    Gender.NOT_KNOWN       | _
    UserType.ADMINISTRATOR | _
    UserType.OPERATOR      | _
    UserType.OPERATOR      | _
  }


  def "カバレッジツールがスキップしてくれればいいのに。。。"() {
    expect:
    new CodeEnum.Filters() instanceof CodeEnum.Filters
  }
}
