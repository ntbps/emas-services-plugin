package com.aliyun.ams.emasservices

import org.gradle.api.GradleException

import javax.annotation.Nullable

class EmasServicesException extends GradleException {

    public EmasServicesException(String message) {
        super(message)
    }

    public EmasServicesException(String message, @Nullable Throwable cause) {
        super(message, cause)
    }
}