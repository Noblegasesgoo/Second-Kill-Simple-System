package com.zhao.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/14 15:50
 * @description
 */

@SpringBootApplication
@MapperScan("com.zhao.seckill.mapper")
public class SecKillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecKillApplication.class, args);
    }

}
