package com.aliyun.ams.emasservices.parser

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.gradle.api.GradleException

class EmasOptionalStringParser implements EmasConfigParser {
    private String configurationKey
    private String resName

    EmasOptionalStringParser(String configurationKey, String resName) {
        this.configurationKey = configurationKey
        this.resName = resName
    }

    @Override
    void parse(JsonObject jsonObject, EmasConfigHandler handler) {
        JsonPrimitive primitive = jsonObject.getAsJsonPrimitive(configurationKey)
        if (primitive != null && handler != null) {
            handler.handleEmasConfig(resName, primitive.asString)
        }
    }
}
