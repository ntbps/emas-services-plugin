package com.aliyun.ams.emasservices.parser

import com.aliyun.ams.emasservices.EmasServicesException
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

class EmasRequiredStringParser implements EmasConfigParser {
    private String configurationKey
    private String resName
    private String exceptionMessage

    EmasRequiredStringParser(String configurationKey, String resName, String exceptionMessage) {
        this.configurationKey = configurationKey
        this.resName = resName
        this.exceptionMessage = exceptionMessage
    }

    @Override
    void parse(JsonObject jsonObject, EmasConfigHandler handler) {
        JsonPrimitive primitive = jsonObject.getAsJsonPrimitive(configurationKey)
        if (primitive == null) {
            throw new EmasServicesException(exceptionMessage)
        } else {
            if (handler != null) {
                handler.handleEmasConfig(resName, primitive.asString)
            }
        }
    }
}
