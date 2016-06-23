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
import java.util.stream.Stream;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.github.ryotan.code.Code;
import io.github.ryotan.code.CodeEnum;
import io.github.ryotan.code.CodeEnum.Filters;
import io.github.ryotan.code.util.CodeEnumReflectionUtil;

@Target({METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {CodeValue.CodeValueValidator.class, CodeValue.CodeEnumValidator.class})
public @interface CodeValue {

    String message() default "{pw.itr0.poc.code.constraint.CodeValue.message}";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};

    Class<? extends CodeEnum<?>> value();

    String[] filters() default {};

    abstract class CodeEnumValidatorSupport<T> implements ConstraintValidator<CodeValue, T> {
        private Class<? extends CodeEnum> code;
        private Predicate filter;

        @Override
        @SuppressWarnings("unchecked")
        public void initialize(CodeValue constraint) {
            this.code = CodeEnumReflectionUtil.getCodeEnumClass(constraint.value());
            this.filter = Stream.of(constraint.filters()).map(s -> CodeEnumReflectionUtil.getCodeFilter(code, s))
                    .reduce(Filters.ANY, Predicate::and);
        }

        @SuppressWarnings("unchecked")
        boolean isValidAsString(String value) {
            return Code.contains(this.code, value, this.filter);
        }
    }

    class CodeValueValidator extends CodeEnumValidatorSupport<String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return super.isValidAsString(value);
        }
    }

    class CodeEnumValidator extends CodeEnumValidatorSupport<CodeEnum> {

        @Override
        public boolean isValid(CodeEnum value, ConstraintValidatorContext context) {
            final boolean isValid = value != null && super.isValidAsString(value.value());
            if (!isValid) {
                context.buildConstraintViolationWithTemplate("{pw.itr0.poc.code.constraint.CodeValue.message.convertFailure}")
                        .addBeanNode()
                        .inIterable()
                        .addConstraintViolation();
                context.disableDefaultConstraintViolation();
            }
            return isValid;
        }
    }
}
