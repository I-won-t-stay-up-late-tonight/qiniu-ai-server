package com.qiniuai.chat.web.controller.advice;

import com.google.common.base.Throwables;
import com.hnit.server.dto.ApiResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Eureka <liangfengyuan1024@gmail.com>
 * @since 2025/9/27 0:49
 */
@Slf4j
@ControllerAdvice
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String EMPTY_JSON = "{}";

  private static final int FAILED = -1;

  /**
   * 处理绑定属性效验不通过异常
   */
  @ExceptionHandler(value = {BindException.class, MethodArgumentNotValidException.class,
      ConstraintViolationException.class})
  public ApiResult<String> bindExceptionErrorHandler(Exception e) {
    logErr(e); // 这里调用的是只有一个参数的方法
    ApiResult<String> ex;
    if (e instanceof BindException) {
      ex = new ApiResult<>(FAILED,
          Objects.requireNonNull(((BindException) e).getFieldError()).getDefaultMessage());
    } else {
      ex = new ApiResult<>(FAILED, e.getMessage());
    }
    ex.setData(EMPTY_JSON);
    return ex;
  }

  /**
   * 请求不合法处理
   */
  @ExceptionHandler(value = {ServletException.class, HttpMessageNotReadableException.class})
  public ApiResult<String> servletExceptionHandler(Exception e) {
    logErr(e); // 这里调用的是只有一个参数的方法
    ApiResult<String> msg = new ApiResult<>(FAILED, "Illegal request");
    msg.setData(EMPTY_JSON);
    return msg;
  }

  /**
   * ClientAbortException
   */
  @ExceptionHandler(value = ClientAbortException.class)
  public void handler(ClientAbortException e) {
    logErr(e); // 这里调用的是只有一个参数的方法
  }

  /**
   * 处理绑定属性效验不通过异常
   */
  @ExceptionHandler(value = RuntimeException.class)
  public ApiResult<String> runtimeExceptionErrorHandler(Exception ex, HttpServletRequest request) {
    printErr(ex, request);
    ApiResult<String> msg = new ApiResult<>(500, "系统异常");
    msg.setData(EMPTY_JSON);
    return msg;
  }

  /**
   * 处理绑定属性效验不通过异常
   */
  @ExceptionHandler(value = Exception.class)
  public ApiResult<String> exceptionErrorHandler(Exception ex, HttpServletRequest request) {
    printErr(ex, request); // 这里应该调用两个参数的方法
    ApiResult<String> msg = new ApiResult<>(FAILED, "服务器异常");
    msg.setData(EMPTY_JSON);
    return msg;
  }

  /**
   * 出错后打印交易记录与请求头
   */
  private void printErr(Exception e, HttpServletRequest request) {
    log.error(
        "[{}] | 服务器异常, 请求路径: {}, 方法: {}",
        Throwables.getStackTraceAsString(e),
        request.getRequestURI(),
        request.getMethod()
    );
  }

  /**
   * 简化版的错误日志记录（只有一个参数）
   */
  private void logErr(Exception e) {
    log.error(
        "[{}] | unexpected error: {}",
        Throwables.getStackTraceAsString(e),
        e.getMessage()
    );
  }

  /**
   * 详细版的错误日志记录（两个参数）
   */
  private void logErr(Exception e, HttpServletRequest request) {
    log.error(
        "[{}] | unexpected error: {} | 请求路径: {}, 方法: {}",
        Throwables.getStackTraceAsString(e),
        e.getMessage(),
        request.getRequestURI(),
        request.getMethod()
    );
  }
}