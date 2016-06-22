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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.github.ryotan.code.Code;
import io.github.ryotan.code.CodeEnum;
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
        private List<Predicate<?>> filters = new ArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public void initialize(CodeValue constraint) {
            this.code = CodeEnumReflectionUtil.getCodeEnumClass(constraint.value());
            for (String s : constraint.filters()) {
                filters.add(CodeEnumReflectionUtil.getCodePattern(code, s));
            }
        }

        @SuppressWarnings("unchecked")
        private boolean isValidAsString(String value) {
            Predicate predicate = CodeEnum.Filters.ANY;
            for (Predicate p : filters) {
                predicate = predicate.and(p);
            }
            return Code.contains(code, value, predicate);
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
