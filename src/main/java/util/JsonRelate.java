package util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonRelate {
    // public static boolean readJsonFromFile(String filename, rapidjson::Document document);
    // boolean writeJsonToFile(String filename,  rapidjson::Document document);
    public static String readJsonData(String pactFile) {
        StringBuilder strBuffer = new StringBuilder();
        File myFile = new File(pactFile);
        if (!myFile.exists()) {
            System.err.println("Can't Find " + pactFile);
        }
        try {
            FileInputStream fis = new FileInputStream(pactFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader in  = new BufferedReader(inputStreamReader);

            String str;
            while ((str = in.readLine()) != null) {
                strBuffer.append(str);
            }
            in.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
        return strBuffer.toString();
    }

    public static JSONObject getJsonMemberObject(JSONObject object, String name) throws Exception {
        JSONObject value = null;
        try {
            value = object.getJSONObject(name);
            if (value == null) {
                throw new JsonMemberMiss(name);
            }
        } catch (JsonMemberMiss e) {
            e.printStackTrace();
        }
        return value;
    }

    public static JSONArray getJsonMemberArray(JSONObject object, String name) throws Exception {
        JSONArray value = null;
        try {
            value = object.getJSONArray(name);
            if (value == null) {
                throw new JsonMemberMiss(name);
            }
        } catch (JsonMemberMiss e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getStringFromJsonObject(JSONObject object, String name) throws Exception {
        String value = null;
        try {
            value = object.getString(name);
            if (value == null) {
                throw new JsonMemberMiss(name);
            }
        } catch (JsonMemberMiss e) {
            e.printStackTrace();
        }
        return value;
    }

    public static int getIntFromJsonObject(JSONObject object, String name) throws Exception {
        Integer value = null;
        try {
            value = object.getInteger(name);
            if (value == null) {
                throw new JsonMemberMiss(name);
            }
        } catch (JsonMemberMiss e) {
            e.printStackTrace();
        }
        return value;

    }

    public static double getDoubleFromJsonObject(JSONObject object, String name) throws Exception {
        Double value = null;
        try {
            value = object.getDouble(name);
            if (value == null) {
                throw new JsonMemberMiss(name);
            }
        } catch (JsonMemberMiss e) {
            e.printStackTrace();
        }
        return value;
    }

    public static boolean getBooleanFromJsonObject(JSONObject object, String name) throws Exception {
        Boolean value = null;
        try {
            value = object.getBoolean(name);
            if (value == null) {
                throw new JsonMemberMiss(name);
            }
        } catch (JsonMemberMiss e) {
            e.printStackTrace();
        }
        return Boolean.TRUE.equals(value);
    }

    public static String getStringFromJsonArray(JSONArray array, int p) throws Exception {
        return array.getString(p);
    }

    public static int getIntFromJsonArray(JSONArray array, int p) throws Exception {
        return array.getInteger(p);
    }

    public static double getDoubleFromJsonArray(JSONArray array, int p) throws Exception {
        return array.getDouble(p);
    }

    public static boolean getBooleanFromJsonArray(JSONArray array, int p) throws Exception {
        return array.getBoolean(p);
    }
}
