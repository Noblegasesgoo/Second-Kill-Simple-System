package com.zhao.seckill;

import com.zhao.seckill.utils.UserUtil;
import org.junit.Test;

public class SeckillApplicationTests {

    @Test
    public void testUserUtil() throws Exception {

        new UserUtil().createUserAndToken(5000);
        System.out.println("完毕");

    }

}
