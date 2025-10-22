package org.hcmu.hcmuserver.handler;

import org.apache.catalina.connector.ClientAbortException;
import org.hcmu.hcmucommon.exception.ServiceException;
import org.hcmu.hcmucommon.result.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;


/**
 * 全局异常处理
 */
@ControllerAdvice
public class ExceptionMaster {

    //    如果捕获到 AccessDeniedException 则 向上抛出
    @ExceptionHandler(AccessDeniedException.class)
    public void accessDeniedException(AccessDeniedException e) throws AccessDeniedException {
        throw e;
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result serviceException(ServiceException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ClientAbortException.class)
    @ResponseBody
    public ResponseEntity clientAbortException(ClientAbortException e) {
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result methodArgumentNotValidException(MethodArgumentNotValidException e) {
        return Result.error(e.getBindingResult().getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(",")));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result exception(Exception e) {
        e.printStackTrace();
        return Result.error(e.getMessage());
    }
}
