package util;

import com.alibaba.fastjson.JSONException;

public class JsonTypeError extends JSONException {
    public JsonTypeError() {}

    public JsonTypeError(String name, String type) {
        super(name + ": expected type " + type);
    }

    public JsonTypeError(String name, String type, Throwable cause) {
        super(name + ": expected type " + type, cause);
    }
}
