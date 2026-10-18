package com.question.seckillagent.common;

import lombok.Data;

//标准化返回一个result，统一返回值的规范
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    //带数据的值正常返回
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("start success");
        result.setData(data);
        return result;
    }

    //返回失败的函数调用
    public static <T> Result<T> error(String message){
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    //不带数据的函数正常调用
    public static <T> Result<T> success(){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(null);
        return result;
    }
}
