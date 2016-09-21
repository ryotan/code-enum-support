/*
 * These codes are licensed under CC0.
 *
 * http://creativecommons.org/publicdomain/zero/1.0/deed
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

package io.github.ryotan.code.constraint;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Predicate;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import io.github.ryotan.code.Code;
import io.github.ryotan.code.CodeEnum;
import io.github.ryotan.code.CodeEnum.Filters;
import io.github.ryotan.code.util.CodeEnumReflectionUtil;

/**
 * {@link CodeEnum}の値であることをあらわす制約です。
 * <p>
 * {@link String}あるいは{@link CodeEnum}の型のフィールドに対して{@link CodeValue}アノテーションを付けておくと、
 * {@link CodeValue#value()}に指定した{@code CodeEnum}に含まれるかどうかの精査が実行されます。
 * 制約を満たさない場合は、{@link CodeValue#message()}に指定されたメッセージの{@link javax.validation.ConstraintViolation}を生成します。
 * デフォルトのメッセージは、{@value DEFAULT_MESSAGE}です。
 * </p>
 * <p>
 * {@link CodeValue#filters()}を指定した場合、指定されたフィルタでフィルタリングされたコード値に精査対象の値が含まれるかどうかを精査します。
 * </p>
 * <p>
 * {@link CodeEnum}型に対してこの制約を利用する場合、{@link CodeValue#value()}は、対象の型と同じクラスを指定してください。
 * </p>
 *
 * @author ryotan
 */
@Target({METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {CodeValue.CodeValueValidator.class, CodeValue.CodeEnumValidator.class})
public @interface CodeValue {
    /**
     * デフォルトのエラーメッセージ。
     */
    String DEFAULT_MESSAGE = "{io.github.ryotan.code.constraint.CodeValue.message}";

    /**
     * 指定されたコード値が{@code value}で指定されたコードのコード値に含まれない場合のエラーメッセージを指定してください。
     *
     * @return エラーメッセージ
     * @see Constraint
     */
    String message() default DEFAULT_MESSAGE;

    /**
     * @return 精査対象となるグループの配列
     * @see Constraint
     */
    Class<?>[] groups() default {};

    /**
     * @return {@link Payload}s for extensibility purposes
     * @see Constraint
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * @return 制約の対象とする {@link CodeEnum}。
     */
    Class<? extends CodeEnum<?>> value();

    /**
     * 制約の対象とする{@link CodeEnum}をフィルタリングするためのメソッド名を指定します。複数指定された場合、AND条件として扱われます。
     * <p>
     * メソッド名には、{@link io.github.ryotan.code.CodeEnum.Filter}アノテーションが付与されているメソッドを指定してください。
     * </p>
     *
     * @return フィルタメソッド名の配列
     */
    String[] filters() default {};

    /**
     * {@link CodeValue}の制約を満しているかどうかを精査する{@link ConstraintValidator}用のサポートクラスです。
     *
     * @param <T> 精査対象の型
     *
     * @author ryotan
     */
    abstract class CodeEnumValidatorSupport<T> implements ConstraintValidator<CodeValue, T> {
        /**
         * 制約の対象となっている{@link CodeEnum}。
         */
        private Class<? extends CodeEnum<?>> code;
        /**
         * {@link CodeEnum}のフィルタリングする{@link Predicate}。
         */
        private Predicate<? super CodeEnum<?>> filter = Filters.ANY;

        /**
         * {@inheritDoc}
         *
         * @throws IllegalArgumentException {@link CodeValue#value()}が有効な{@link CodeEnum}でない場合。
         *                                  もしくは、{@link CodeValue#filters()}で指定されたフィルタメソッドが{@link CodeValue#value()}に存在しない場合。
         */
        @Override
        @SuppressWarnings("unchecked")
        public void initialize(CodeValue constraint) {
            this.code = CodeEnumReflectionUtil.getCodeEnumClass(constraint.value());
            for (String filter : constraint.filters()) {
                this.filter = CodeEnumReflectionUtil.getCodeFilter((Class) this.code, filter).and(this.filter);
            }
        }

        /**
         * コード値{@code value}が{@link CodeValue#value()}で指定されたコードに存在するかどうかを精査します。
         * {@link CodeValue#filters()}が指定されている場合、指定されたフィルタメソッドでコードをフィルタリングしたうえで、
         * 含まれているかどうかを精査します。
         * <p>
         * {@code value}が{@code null}の場合には、{@code true}を返します。
         * </p>
         *
         * @param value 精査対象の値
         * @return {@code value}が制約を満たす場合、もしくは{@code value}が{@code null}の場合、{@code true}
         */
        @SuppressWarnings("unchecked")
        protected boolean isValidAsString(String value) {
            return value == null || Code.contains((Class) this.code, value, this.filter);
        }
    }

    /**
     * {@link String}型の値に対して{@link CodeValue}の制約を満たしているかどうかを精査する{@link ConstraintValidator}です。
     * <p>
     * 精査対象の値は、{@link CodeEnum}のEnum名ではなく、コード値({@link CodeEnum#value()})として解釈されます。
     * </p>
     *
     * @author ryotan
     */
    class CodeValueValidator extends CodeEnumValidatorSupport<String> {
        /**
         * {@inheritDoc}
         *
         * @see CodeEnumValidatorSupport#isValidAsString(String)
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return super.isValidAsString(value);
        }
    }

    /**
     * {@link CodeEnum}型の値に対して{@link CodeValue}の制約を満たしているかどうかを精査する{@link ConstraintValidator}です。
     * <p>
     * すでに{@link CodeEnum}として変換されている値が精査対象となるため、{@link CodeValue#filters()}を指定しなければ、常に{@code true}を返すでしょう。
     * </p>
     *
     * @author ryotan
     */
    class CodeEnumValidator extends CodeEnumValidatorSupport<CodeEnum> {
        @Override
        public boolean isValid(CodeEnum value, ConstraintValidatorContext context) {
            return value == null || value.getClass().equals(super.code) && super.isValidAsString(value.value());
        }
    }
}
