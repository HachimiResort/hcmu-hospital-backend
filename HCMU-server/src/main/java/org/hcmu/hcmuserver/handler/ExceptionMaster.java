package org.hcmu.hcmuserver.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.catalina.connector.ClientAbortException;
import org.hcmu.hcmucommon.exception.ServiceException;
import org.hcmu.hcmucommon.result.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public Result httpMessageNotReadableException(HttpMessageNotReadableException e) {
        // 处理枚举类型反序列化错误
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) e.getCause();
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String fieldName = ife.getPath().get(0).getFieldName();
                Object invalidValue = ife.getValue();
                Class<?> enumClass = ife.getTargetType();

                // 获取枚举的所有有效值
                Object[] enumConstants = enumClass.getEnumConstants();
                StringBuilder validValues = new StringBuilder();
                for (int i = 0; i < enumConstants.length; i++) {
                    validValues.append(enumConstants[i].toString());
                    if (i < enumConstants.length - 1) {
                        validValues.append("、");
                    }
                }

                return Result.error(String.format("参数 '%s' 的值 '%s' 无效，必须为 %s 中的一个",
                    fieldName, invalidValue, validValues.toString()));
            }
        }
        return Result.error("请求参数格式错误：" + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result exception(Exception e) {
        e.printStackTrace();
        return Result.error(e.getMessage());
    }
}
