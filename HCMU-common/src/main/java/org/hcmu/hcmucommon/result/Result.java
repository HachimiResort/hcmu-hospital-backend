package org.hcmu.hcmucommon.result;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义的响应数据结构
 * @param <T>
 */
@Data
@NoArgsConstructor
public class Result<T> {
    private Integer code; // 编码：200成功，其它数字为失败
    private String msg; // 错误信息
    private T data; // 数据

//    私有化构造方法
    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T object) {
        return new Result<T>(200, "响应成功!", object);
    }

    public static <T> Result<T> success(String msg, T object){
        return new Result<T>(200, msg, object);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<T>(500, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<T>(code, msg, null);
    }
}

