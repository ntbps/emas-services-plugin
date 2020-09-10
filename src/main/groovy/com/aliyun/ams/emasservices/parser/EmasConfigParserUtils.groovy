package com.aliyun.ams.emasservices.parser

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

final class EmasConfigParserUtils {
    private static final Logger logger = Logging.getLogger(EmasConfigParserUtils.class.simpleName)

    static Map<String, String> getConfigResources(JsonObject rootObject) {
        JsonObject configInfo = rootObject.getAsJsonObject("config")
        if (configInfo == null)
            throw new GradleException("Missing config object")

        Map<String, String> resValues = new TreeMap<>()
        EmasConfigHandler configHandler = new EmasConfigHandler() {
            @Override
            void handleEmasConfig(String resName, Object resValue) {
                if (resValue instanceof String) {
                    logger.info("handle: ${resName}:${resValue}")
                    resValues.put(resName, resValue as String)
                }
            }
        }
        Arrays.asList(
                new EmasRequiredStringParser("emas.appKey", "ams_appKey", "Missing config/emas.app_key object"),
                new EmasRequiredStringParser("emas.appSecret", "ams_appSecret", "Missing config/emas.app_secret object"),
                new EmasRequiredStringParser("emas.packageName", "ams_packageName", "Missing config/emas.package_name object"),
                new EmasOptionalStringParser("httpdns.secretKey", "ams_httpdns_secretKey"),
                new EmasOptionalStringParser("httpdns.accountId", "ams_accountId"),
                new EmasOptionalStringParser("hotfix.idSecret", "ams_hotfix_idSecret"),
                new EmasOptionalStringParser("hotfix.rsaSecret", "ams_hotfix_rsaSecret"),
        ).forEach { parser ->
            parser.parse(configInfo, configHandler)
        }
        return resValues
    }

    static String getPackageName(JsonObject rootObject, String applicationId) {
        JsonObject configInfo = rootObject.getAsJsonObject("config")
        if (configInfo == null)
            throw new GradleException("Missing config object")
        JsonPrimitive primitive = configInfo.getAsJsonPrimitive("emas.packageName")
        if (primitive == null) {
            throw new GradleException("Missing config/emas.package_name object, desired:${applicationId}")
        }
        return primitive.asString
    }
}
