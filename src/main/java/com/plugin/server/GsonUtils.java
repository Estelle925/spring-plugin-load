package com.plugin.server;

import com.google.gson.Gson;

public class GsonUtils {

    private static final Gson GSON = new Gson();

    private GsonUtils() {
        throw new UnsupportedOperationException();
    }

    public static String toJson(Object src) {
        return GSON.toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

}
