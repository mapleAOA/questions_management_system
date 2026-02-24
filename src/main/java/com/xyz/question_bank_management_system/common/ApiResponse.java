package com.xyz.question_bank_management_system.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缁熶竴杩斿洖浣擄細闄ょ櫥褰曟帴鍙ｅ锛屽叾浠栨帴鍙ｇ粺涓€鐢ㄨ缁撴瀯銆? */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String msg;
    private String code;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.msg = "ok";
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> ok(String msg, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.msg = msg;
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> fail(String code, String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.code = code;
        r.msg = msg;
        return r;
    }

    public static ApiResponse<Void> ok() {
        ApiResponse<Void> r = new ApiResponse<>();
        r.success = true;
        r.msg = "ok";
        r.data = null;
        return r;
    }
}
