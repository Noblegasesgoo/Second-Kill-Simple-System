package com.zhao.seckill.validator;

import com.zhao.seckill.common.annotations.IsMobile;
import com.zhao.seckill.utils.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/12 17:17
 * @description 手机号码校验规则
 */


public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {

        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        /** 判断传入值是否必填 **/
        if (this.required) {
            return ValidatorUtil.isMobile(value);
        } else {
            if (StringUtils.isEmpty(value)) {
                return true;
            } else {
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
