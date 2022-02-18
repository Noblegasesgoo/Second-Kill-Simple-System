package com.zhao.seckill.handler;

import com.zhao.seckill.common.enums.StatusCode;
import com.zhao.seckill.controller.response.Response;
import com.zhao.seckill.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLIntegrityConstraintViolationException;


/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/12 17:34
 * @description 全局异常处理类
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Response exceptionHandler(Exception e) {

        if (e instanceof GlobalException) {

            GlobalException ex = (GlobalException) e;
            return Response.error();
        } else if (e instanceof BindException) {

            BindException ex = (BindException) e;
            return Response.setResponse(StatusCode.VALID_ERROR);
        }

        return Response.setResponse(StatusCode.STATUS_CODEC500);
    }

    @ExceptionHandler(value = BindException.class)
    public Response handleException(BindException e){
        log.error(e.getMessage());
        return Response.setResponse(StatusCode.VALID_ERROR);
    }


    @ExceptionHandler(value = SQLIntegrityConstraintViolationException.class)
    public Response handleException(SQLIntegrityConstraintViolationException e){
        log.error(e.getMessage());
        return Response.error().message("SQL完整性约束异常(外键引起)");
    }

    /**
     * BadSql异常处理
     * @param e
     * @return Response
     */
    @ExceptionHandler(value = BadSqlGrammarException.class)
    public Response handleException(BadSqlGrammarException e){
        log.error(e.getMessage());
        return Response.setResponse(StatusCode.BAD_SQL_GRAMMAR_ERROR);
    }

    @ExceptionHandler(value = GlobalException.class)
    public Response handleException(GlobalException e){
        return Response.error().message(e.getMessage()).code(e.getCode());
    }

    /**
     * Controller上一层相关异常
     * @param e
     * @return Response
     */
    @ExceptionHandler({
            NoHandlerFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMessageNotWritableException.class,
            MethodArgumentNotValidException.class,
            HttpMediaTypeNotAcceptableException.class,
            ServletRequestBindingException.class,
            ConversionNotSupportedException.class,
            MissingServletRequestPartException.class,
            AsyncRequestTimeoutException.class
    })
    public Response handleServletException(Exception e) {
        log.error(e.getMessage(), e);
        return Response.error().message(StatusCode.SERVLET_ERROR.getMessage()).code(StatusCode.SERVLET_ERROR.getCode());
    }
}
