package com.aliyun.ams.emasservices

import org.gradle.api.tasks.Input

class ServiceEntity {
    private String name

    private int status

    private String version

    @Input
    String getName() {
        return this.name
    }

    void setName(String name) {
        this.name = name
    }

    int getStatus() {
        return this.status
    }

    @Input
    void setStatus(int status) {
        this.status = status
    }

    String getVersion() {
        return this.version
    }

    @Input
    void setVersion(String version) {
        this.version = version
    }
}