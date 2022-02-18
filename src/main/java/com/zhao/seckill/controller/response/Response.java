package com.zhao.seckill.controller.response;

import com.zhao.seckill.common.enums.StatusCode;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/5 15:42
 * @description
 */

@Data
public class Response {

    /** 状态码 **/
    private Integer code;

    /** 提示信息 **/
    private String message;

    /** 响应数据 **/
    private Map<String,Object> mapData = new HashMap<>();
    private Object data;

    /** 构造函数私有化 **/
    private Response(){};

    /**
     * @return 返回成功结果
     */
    public static Response success(){
        Response r = new Response();
        r.setCode(StatusCode.STATUS_CODEC200.getCode());
        r.setMessage(StatusCode.STATUS_CODEC200.getMessage());

        return r;
    }

    /**
     * @return 返回失败结果
     */
    public static Response error(){
        Response r = new Response();
        r.setCode(StatusCode.STATUS_CODEC_MINUS200.getCode());
        r.setMessage(StatusCode.STATUS_CODEC_MINUS200.getMessage());

        return r;
    }

    /**
     * 设置特定结果
     * @param result
     * @return 特定结果
     */
    public static Response setResponse(StatusCode result){
        Response r = new Response();
        r.setCode(result.getCode());
        r.setMessage(result.getMessage());

        return r;
    }

    /**
     * 设置返回值(返回this方便链式编程)
     * @param value
     * @return 特定结果
     */
    public Response data(Object value){

        this.data = value;
        return this;
    }

    /**
     * 设置返回值(返回this方便链式编程)
     * @param map
     * @return 特定结果
     */
    public Response data(Map<String,Object> map){

        this.setData(map);
        return this;
    }

    public Response message(String message){

        this.setMessage(message);
        return this;
    }

    public Response code(Integer code){

        this.setCode(code);
        return this;
    }
}
