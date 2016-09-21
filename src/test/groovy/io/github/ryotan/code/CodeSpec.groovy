/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code

import io.github.ryotan.code.example.Gender
import io.github.ryotan.code.example.NotEnum
import io.github.ryotan.code.example.UserType
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Specification class of {@link Code}
 */
@Unroll
class CodeSpec extends Specification {
  def "Code.ofはCodeEnumのインスタンスを返却すること。"() {
    when:
    def actual = Code.of(eClass, value)

    then:
    actual == code

    where:
    eClass   | value | code
    Gender   | "1"   | Gender.MALE
    UserType | "3"   | UserType.MEMBER
  }

  def "Code.ofは、CodeEnumにvalueが存在しない場合、IllegalArgumentExceptionを送出すること。"() {
    when:
    Code.of(eClass, value)

    then:
    def exception = thrown(IllegalArgumentException)
    exception.message =~ /${eClass.name}/
    exception.message =~ /${value}/

    where:
    eClass   | value
    Gender   | "no"
    UserType | "99"
  }

  def "filterが指定された場合、Code.ofはフィルタリング後のCodeEnumにvalueが存在しない場合、IllegalArgumentExceptionを送出すること。"() {
    when:
    def e = Code.of(eClass, value)
    Code.of(eClass, value, filter)

    then:
    e == code
    def exception = thrown(IllegalArgumentException)
    exception.message =~ /${eClass.name}/
    exception.message =~ /${value}/

    where:
    eClass   | value | code            | filter
    Gender   | "1"   | Gender.MALE     | CodeEnum.Filters.exclude(Gender.MALE)
    UserType | "3"   | UserType.MEMBER | CodeEnum.Filters.include(UserType.ADMINISTRATOR)
  }

  def "Code.ofは、Enumでないクラスが渡された場合、IllegalArgumentExceptionを送出すること。"() {
    when:
    Code.of(eClass, value)

    then:
    def exception = thrown(IllegalArgumentException)
    exception.message =~ /${eClass.name}/
    exception.message =~ /must be enum/
    exception.message != ~/${value}/

    where:
    eClass  | value
    NotEnum | "no"
  }

  def "Code.orはCodeEnumのインスタンスを返却すること。"() {
    when:
    def actual = Code.or(eClass, value, defaultValue)

    then:
    actual == code

    where:
    eClass   | value | code            | defaultValue
    Gender   | "1"   | Gender.MALE     | Gender.NOT_KNOWN
    UserType | "3"   | UserType.MEMBER | UserType.OPERATOR
  }

  def "Code.orは、CodeEnumにvalueが存在しない場合、指定されたデフォルト値を返すこと。"() {
    when:
    def actual = Code.or(eClass, value, defaultValue)

    then:
    actual == defaultValue

    where:
    eClass   | value | defaultValue
    Gender   | "no"  | Gender.NOT_KNOWN
    UserType | "99"  | UserType.OPERATOR
  }

  def "filterが指定された場合、Code.orはフィルタリング後のCodeEnumにvalueが存在しない場合、指定されたデフォルト値を返すこと。"() {
    when:
    def e = Code.or(eClass, value, defaultValue)
    def actual = Code.or(eClass, value, defaultValue, filter)

    then:
    e == code
    actual == defaultValue

    where:
    eClass   | value | code            | defaultValue      | filter
    Gender   | "1"   | Gender.MALE     | Gender.NOT_KNOWN  | CodeEnum.Filters.exclude(Gender.MALE)
    UserType | "3"   | UserType.MEMBER | UserType.OPERATOR | CodeEnum.Filters.include(UserType.ADMINISTRATOR)
  }

  def "Code.orは、Enumでないクラスが渡された場合、IllegalArgumentExceptionを送出すること。"() {
    when:
    Code.or(eClass, value, defaultValue)

    then:
    def exception = thrown(IllegalArgumentException)
    exception.message =~ /${eClass.name}/
    exception.message =~ /must be enum/
    exception.message != ~/${value}/
    exception.message != ~/${defaultValue}/

    where:
    eClass  | value | defaultValue
    NotEnum | "no"  | new NotEnum()
  }

  def "Code.valuesは、CodeEnumに定義されている全てのコードのリストをEnum.ordinal()でソートして返すこと"() {
    expect:
    Code.values(eClass) == expected

    where:
    eClass   | expected
    Gender   | [Gender.NOT_APPLICABLE, Gender.FEMALE, Gender.MALE, Gender.NOT_KNOWN]
    UserType | [UserType.ADMINISTRATOR, UserType.OPERATOR, UserType.MEMBER]
  }

  def "Code.valuesは、filterが指定された場合、CodeEnumに定義されているコードのフィルタリングされたリストをEnum.ordinal()でソートして返すこと"() {
    expect:
    Code.values(eClass, filter) == expected

    where:
    eClass   | filter                                                            | expected
    Gender   | Gender.JIS_X0303                                                  | [Gender.FEMALE, Gender.MALE]
    UserType | CodeEnum.Filters.include(UserType.ADMINISTRATOR, UserType.MEMBER) | [UserType.ADMINISTRATOR, UserType.MEMBER]
  }

  def "Code.valuesは、sorterが指定された場合、CodeEnumに定義されている全てのコードのリストをsorterでソートして返すこと"() {
    expect:
    Code.values(eClass, sorter) == expected

    where:
    eClass   | sorter                                                  | expected
    Gender   | { c1, c2 -> c2.ordinal() - c1.ordinal() } as Comparator | [Gender.NOT_KNOWN, Gender.MALE, Gender.FEMALE, Gender.NOT_APPLICABLE]
    UserType | { c1, c2 -> c2.ordinal() - c1.ordinal() } as Comparator | [UserType.MEMBER, UserType.OPERATOR, UserType.ADMINISTRATOR]
  }

  def "Code.valuesは、filterとsorterが指定された場合、CodeEnumに定義されているコードのフィルタリングされたリストをsorterでソートして返すこと"() {
    expect:
    Code.values(eClass, filter, sorter) == expected

    where:
    eClass   | filter                                                            | sorter                                                  | expected
    Gender   | Gender.JIS_X0303                                                  | { c1, c2 -> c2.ordinal() - c1.ordinal() } as Comparator | [Gender.MALE, Gender.FEMALE]
    UserType | CodeEnum.Filters.include(UserType.ADMINISTRATOR, UserType.MEMBER) | { c1, c2 -> c2.ordinal() - c1.ordinal() } as Comparator | [UserType.MEMBER, UserType.ADMINISTRATOR]
  }

  def "Code.valuesは、Enumでないクラスが渡された場合、IllegalArgumentExceptionを送出すること。"() {
    when:
    Code.values(eClass)

    then:
    def exception = thrown(IllegalArgumentException)
    exception.message =~ /${eClass.name}/
    exception.message =~ /must be enum/

    where:
    eClass  | _
    NotEnum | _
  }

  def "Code.containsは、コードにコード値が一致するものが存在する場合のみtrueを返すこと。"() {
    expect:
    Code.contains(eClass, value) == expected

    where:
    eClass   | value                          | expected
    Gender   | Gender.MALE.value()            | true
    Gender   | "not existing value"           | false
    UserType | UserType.ADMINISTRATOR.value() | true
    UserType | "not existing value"           | false
  }

  def "Code.containsは、filterが指定された場合はfilterを満たすコードにコード値が一致するものが存在する場合のみtrueを返すこと。"() {
    expect:
    Code.contains(eClass, value, filter) == expected

    where:
    eClass   | value                          | filter                                                            | expected
    Gender   | Gender.MALE.value()            | Gender.JIS_X0303                                                  | true
    Gender   | Gender.NOT_APPLICABLE.value()  | Gender.JIS_X0303                                                  | false
    UserType | UserType.ADMINISTRATOR.value() | CodeEnum.Filters.include(UserType.ADMINISTRATOR, UserType.MEMBER) | true
    UserType | UserType.OPERATOR.value()      | CodeEnum.Filters.include(UserType.ADMINISTRATOR, UserType.MEMBER) | false
  }

  def "Code.containsは、Enumでないクラスが渡された場合、IllegalArgumentExceptionを送出すること。"() {
    when:
    Code.contains(eClass, value)

    then:
    def exception = thrown(IllegalArgumentException)
    exception.message =~ /${eClass.name}/
    exception.message =~ /must be enum/

    where:
    eClass  | value
    NotEnum | "value"
  }

  def "Code.shortLabelは、指定されたコードのShortLabelアノテーションの値を返すこと。"() {
    expect:
    Code.shortLabel(eClass, value) == expected

    where:
    eClass | value | expected
    Gender | "2"   | "女"
    Gender | "1"   | "男"
  }

  def "Code.shortLabelは、指定されたコードがShortLabelでアノテーションされていない場合、IllegalArgumentExceptionを送出すること。"() {
    when:
    Code.shortLabel(eClass, value)

    then:
    def exception = thrown(IllegalArgumentException)
    exception.message =~ /${eClass.simpleName}.${Code.of(eClass, value)}/

    where:
    eClass   | value
    Gender   | "9"
    UserType | "1"
  }

  def "Code.shortLabelは、Enumでないクラスが渡された場合、IllegalArgumentExceptionを送出すること。"() {
    when:
    Code.shortLabel(eClass, value)

    then:
    def exception = thrown(IllegalArgumentException)
    exception.message =~ /${eClass.name}/
    exception.message =~ /must be enum/

    where:
    eClass  | value
    NotEnum | "value"
  }

  def "Code.aliasは、指定されたコードのフィールドもしくはメソッドでAliasLabelアノテーションがあり、名前がaliasNameに一致するものの値を返すこと。"() {
    expect:
    Code.alias(eClass, value, aliasName) == expected

    where:
    eClass   | value | aliasName       | expected
    Gender   | "9"   | "ENGLISH_LABEL" | "not applicable"
    Gender   | "2"   | "ENGLISH_LABEL" | "female"
    UserType | "1"   | "englishLabel"  | "ADMINISTRATOR"
    UserType | "3"   | "englishLabel"  | "MEMBER"
  }

  def "Code.aliasは、指定されたコードのフィールドもしくはメソッドにAliasLabelアノテーションがあり、名前がaliasNameに一致するものが存在しない場合は、IllegalArgumentExceptionを送出すること。"() {
    when:
    Code.alias(eClass, value, aliasName)

    then:
    def exception = thrown(IllegalArgumentException)
    exception.message =~ /${eClass.simpleName}.${Code.of(eClass, value)}/
    exception.message =~ /${aliasName}/
    exception.message =~ /@AliasLabel/

    where:
    eClass   | value | aliasName
    Gender   | "9"   | "NOT_EXIST_FIELD"
    Gender   | "2"   | "NOT_EXIST_FIELD"
    UserType | "1"   | "notExistMethod"
    UserType | "3"   | "notExistMethod"
  }

  def "Code.aliasは、Enumでないクラスが渡された場合、IllegalArgumentExceptionを送出すること。"() {
    when:
    Code.alias(eClass, value, "alias")

    then:
    def exception = thrown(IllegalArgumentException)
    exception.message =~ /${eClass.name}/
    exception.message =~ /must be enum/

    where:
    eClass  | value
    NotEnum | "value"
  }

  def "カバレッジツールがスキップしてくれればいいのに。。。"() {
    expect:
    new Code() instanceof Code
  }
}
