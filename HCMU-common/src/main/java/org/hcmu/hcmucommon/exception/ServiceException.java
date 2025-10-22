package org.hcmu.hcmucommon.exception;

import lombok.Data;

/**
 * 自定义服务器异常
 */
@Data
public class ServiceException extends RuntimeException {

    private final Integer code;

    public ServiceException(String msg) {
        super(msg);
        this.code = 500;  //默认500
    }

    public ServiceException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

}
