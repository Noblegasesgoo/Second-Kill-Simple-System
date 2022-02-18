package com.zhao.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/1/18 13:27
 * @description swagger2配置类
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {
    /**
     * 规定扫描哪些包下面需要生成swagger文档
     * @return
     */
    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(this.apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.zhao.seckill.controller")) // 要生成文档的包
                .paths(PathSelectors.any()) // 配置路径
                .build()
                // 添加全局授权
                .securityContexts(this.securityContexts()) // 设置安全上下文
                .securitySchemes(this.securitySchemes()); // 设置安全计划
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("秒杀系统接口文档")
                .description("秒杀系统接口文档")
                .contact(new Contact("noblegasesgoo", "http://localhost:1234/doc.html", "26328168232qq.com"))
                .version("v1.0.0")
                .build();
    }

    private List<ApiKey> securitySchemes(){

        // 用来设置请求头信息
        List<ApiKey> result = new ArrayList<>();

        // 参数一：apiKey的名字 二：具体的要准备的key的名字
        ApiKey apiKey = new ApiKey("Authorization", "Authorization", "Header");
        result.add(apiKey);

        return result;
    }


    private List<SecurityContext> securityContexts(){

        // 用来设置需要认证的路径
        List<SecurityContext> result = new ArrayList<>();
        result.add(getContextByPath("/hello/.*"));
        return result;
    }
    /**
     * 用正则匹配的方法得到需要认证的路径
     * @param pathRegex
     * @return
     */
    private SecurityContext getContextByPath(String pathRegex) {
        // 用正则匹配的方法
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex(pathRegex))
                .build();

    }
    /**
     * 获得默认的securityReferences对象。
     * @return List<SecurityReference>
     */
    private List<SecurityReference> defaultAuth() {

        List<SecurityReference> result = new ArrayList<>();

        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything"); // 授权范围

        // 准备一个 AuthorizationScope 数组将上面的 authorizationScope 对象放入数组中
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;

        result.add(new SecurityReference("Authorization", authorizationScopes));

        return result;
    }
}
