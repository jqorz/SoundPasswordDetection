package com.jqorz.soundpassworddetection.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GSON工具类，用以解析json字符串等
 */
public class GsonUtil {
    private static Gson gson = null;

    static {
        if (gson == null) {
            gson = new Gson();
        }
    }

    private GsonUtil() {
    }

    /**
     * 将Json字符串转化为Map<String, Object>对象
     *
     * @param jsonString Json字符串
     * @return Map对象
     */
    public static Map<String, Object> json2Map(String jsonString) {
        Map<String, Object> map = new HashMap<>();
        if (gson != null) {
            map = gson.fromJson(jsonString,
                    new TypeToken<Map<String, Object>>() {
                    }.getType());
        }
        return map;
    }

    /**
     * 将Json字符串转化为List<T>对象
     *
     * @param jsonString Json字符串
     * @param cls        类型 即实体类
     * @return List<T>对象
     */
    public static <T> List<T> json2List(String jsonString, Class<T> cls) {
        if (jsonString == null || TextUtils.isEmpty(jsonString))
            return null;
        List<T> list = new ArrayList<>();
        JsonArray array = new JsonParser().parse(jsonString).getAsJsonArray();
        if (gson != null) {
            for (final JsonElement elem : array) {
                list.add(gson.fromJson(elem, cls));
            }
        }

        return list;
    }

    /**
     * 解析json转为bean
     *
     * @param jsonString json数据
     * @return bean
     */
    public static <T> T Json2Bean(String jsonString, Class<T> cls) {
        T t = null;
        if (gson != null) {
            JsonReader reader = new JsonReader(new StringReader(jsonString));
            reader.setLenient(true);
            t = gson.fromJson(reader, cls);
        }
        return t;
    }


    /**
     * 转成json
     *
     * @param object 对象
     * @return json
     */
    public static String jsonCreate(Object object) {
        String gsonString = null;
        if (gson != null) {
            gsonString = gson.toJson(object);
        }
        return gsonString;
    }

}
