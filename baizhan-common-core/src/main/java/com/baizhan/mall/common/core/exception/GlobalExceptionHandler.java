package com.baizhan.mall.common.core.exception;

import com.baizhan.mall.common.core.result.Result;
import com.baizhan.mall.common.core.result.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** @Valid 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /** 表单绑定 / 路径参数校验失败 */
    @ExceptionHandler({BindException.class, ConstraintViolationException.class})
    public Result<Void> handleBindException(Exception e) {
        String message = e.getMessage();
        if (e instanceof ConstraintViolationException cve) {
            message = cve.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("；"));
        }
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /** 缺少网关透传的用户头（未登录/未走网关） */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<Void> handleMissingHeader(MissingRequestHeaderException e) {
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    /** 请求体不可读 / 参数类型不匹配 / 404 / 405 */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    public Result<Void> handleRequestException(Exception e) {
        return Result.fail(ResultCode.BAD_REQUEST);
    }

    /** 兜底 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("[系统异常]", e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
