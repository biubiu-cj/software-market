package software_market.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 通用返回对象
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    public static <T> Result<T> success(T data){
        return new Result<T>(200,"操作成功",data);
    }

    public static <T> Result<T> success(T data, String message){
        return new Result<T>(200,message,data);
    }
    public static <T> Result<T> fail(T data){
        return new Result<T>(400,"操作失败",data);
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}
