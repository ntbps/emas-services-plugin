package com.aliyun.ams.emasservices.parser

import com.google.gson.JsonObject

interface EmasConfigParser {
    void parse(JsonObject jsonObject, EmasConfigHandler handler)
}