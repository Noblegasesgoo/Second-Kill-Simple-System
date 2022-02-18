package com.zhao.seckill.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/5 18:02
 * @description 校验工具类
 */

@Component
public class ValidatorUtil {

    private static final Pattern mobile_pattern = Pattern.compile("[1]([3-9])[0-9]{9}$");

    public static Boolean isMobile(String mobile) {

        if (StringUtils.isBlank(mobile)) {
            return false;
        }

        Matcher matcher = mobile_pattern.matcher(mobile);
        return matcher.matches();
    }
}
