package com.question.seckillagent.context;

// 把userId放到线程本地存储，在同一个请求里面可以减少传递userId的参数
public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId){
        USER_ID.set(userId);
    }

    public static Long getUserId(){
        return USER_ID.get();
    }

    public static void clear(){
        USER_ID.remove();
    }
}
