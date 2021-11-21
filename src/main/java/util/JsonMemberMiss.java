package util;

import com.alibaba.fastjson.JSONException;

public class JsonMemberMiss extends JSONException {
    public JsonMemberMiss() {
        super();
    }

    public JsonMemberMiss(String message) {
        super(message + " is required but missing in json file");
    }

    public JsonMemberMiss(String message, Throwable cause) {
        super(message + " is required but missing in json file", cause);
    }
}
