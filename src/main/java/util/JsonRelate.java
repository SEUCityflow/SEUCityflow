package util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonRelate {
    public static void writeJsonToFile(String filename, String json) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename));
        bufferedWriter.write(json);
        bufferedWriter.close();
    }

    public static String readJsonData(String pactFile) throws IOException {
        StringBuilder strBuffer = new StringBuilder();
        File myFile = new File(pactFile);
        if (!myFile.exists()) {
            System.err.println("Can't Find " + pactFile);
        }
            FileInputStream fis = new FileInputStream(pactFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(inputStreamReader);

            String str;
            while ((str = in.readLine()) != null) {
                strBuffer.append(str);
            }
            in.close();
        return strBuffer.toString();
    }

    public static JSONObject getJsonMemberObject(JSONObject object, String name) throws JsonMemberMiss {
        JSONObject value;
        value = object.getJSONObject(name);
        if (value == null) {
            throw new JsonMemberMiss(name);
        }
        return value;
    }

    public static JSONArray getJsonMemberArray(JSONObject object, String name) throws JsonMemberMiss {
        JSONArray value;
        value = object.getJSONArray(name);
        if (value == null) {
            throw new JsonMemberMiss(name);
        }
        return value;
    }

    public static String getStringFromJsonObject(JSONObject object, String name) throws JsonMemberMiss {
        String value;
        value = object.getString(name);
        if (value == null) {
            throw new JsonMemberMiss(name);
        }
        return value;
    }

    public static int getIntFromJsonObject(JSONObject object, String name) throws JsonMemberMiss {
        Integer value;
        value = object.getInteger(name);
        if (value == null) {
            throw new JsonMemberMiss(name);
        }
        return value;
    }

    public static double getDoubleFromJsonObject(JSONObject object, String name) throws JsonMemberMiss {
        Double value;
        value = object.getDouble(name);
        if (value == null) {
            throw new JsonMemberMiss(name);
        }
        return value;
    }

    public static boolean getBooleanFromJsonObject(JSONObject object, String name) throws JsonMemberMiss {
        Boolean value;
        value = object.getBoolean(name);
        if (value == null) {
            throw new JsonMemberMiss(name);
        }
        return Boolean.TRUE.equals(value);
    }

    public static String getStringFromJsonArray(JSONArray array, int p) {
        return array.getString(p);
    }

    public static int getIntFromJsonArray(JSONArray array, int p) {
        return array.getInteger(p);
    }

    public static double getDoubleFromJsonArray(JSONArray array, int p) {
        return array.getDouble(p);
    }

    public static boolean getBooleanFromJsonArray(JSONArray array, int p) {
        return array.getBoolean(p);
    }
}
