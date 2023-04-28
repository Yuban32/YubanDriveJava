package com.yuban32.response;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * @author Yuban32
 * @ClassName Result
 * @Description 统一返回模型
 * @Date 2023年02月22日
 */
public class Result extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    /**状态码*/
    public static final String CODE_TAG = "code";

    /**返回内容*/
    public static final String MSG_TAG = "msg";

    /**返回数据对象*/
    public static final String DATA_TAG = "data";

    /**
     *
     * 初始化一个新创建的APIResult对象
     * @param code 状态码
     * @param msg 返回内容
     * @param data 返回数据对象
     * */

    public  Result(int code, String msg, Object data){
        super.put(CODE_TAG,code);
        super.put(MSG_TAG,msg);
        if(data != null){
            super.put(DATA_TAG,data);
        }
    }
    /**
     * 返回成功的消息
     * @return 成功消息
     * */
    public static Result success(){
        return Result.success("操作成功");
    }

    /**
     * 返回成功消息
     * @return 成功消息
     * */
    public static Result success(Object data){
        return Result.success("操作成功",data);
    }

    /**
     * 返回成功消息
     * @param msg 返回内容
     * */
    public static Result success(String msg){
        return Result.success(msg,null);
    }

    /**
     * 返回成功消息
     * @param msg 返回内容
     * @param data 返回数据对象
     * */
    public static Result success(String msg, Object data){
        return new Result(HttpServletResponse.SC_OK, msg, data);
    }

    /**
     * 返回错误消息
     * @return
     */
    public static Result error(){
        return Result.error("操作失败");
    }

    /**
     * 返回错误消息
     * @param msg 返回内容
     * @return 错误信息
     */
    public static Result error(String msg){
        return Result.error(msg,null);
    }

    /**
     * 返回错误消息
     * @param msg 返回内容
     * @param data
     * @return 错误信息
     */
    public static Result error(String msg, Object data){
        return new Result(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg, data);
    }

    /**
     * 返回错误消息
     * @param msg 返回内容
     * @return 错误信息
     */

    public static Result error(int code, String msg){
        return new Result(code, msg, null);
    }
}