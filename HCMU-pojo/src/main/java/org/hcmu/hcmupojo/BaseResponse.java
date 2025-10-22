package org.hcmu.hcmupojo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义的响应数据结构
 * @param <T>
 */
@Data
@NoArgsConstructor
public class BaseResponse<T> {
    private Integer code; // 编码：200成功，其它数字为失败
    private String msg; // 错误信息
    private T data; // 数据

//    私有化构造方法
    private BaseResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> BaseResponse<T> success(T object) {
        return new BaseResponse<T>(200, "响应成功!", object);
    }

    public static <T> BaseResponse<T> success(String msg,T object){
        return new BaseResponse<T>(200, msg, object);
    }

    public static <T> BaseResponse<T> error(String msg) {
        return new BaseResponse<T>(500, msg, null);
    }

    public static <T> BaseResponse<T> error(Integer code,String msg) {
        return new BaseResponse<T>(code, msg, null);
    }
}

