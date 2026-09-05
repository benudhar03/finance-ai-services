package com.finance.ai.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object o) {
        try { return MAPPER.writeValueAsString(o); } catch (Exception e) { return "{}"; }
    }
}
