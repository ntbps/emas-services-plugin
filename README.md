# emas-services-plugin

The Gradle plugin to help with using Aliyun EMAS services SDK(Support multiple product flavros).<br/>
([Main repo: https://github.com/awc/emas-services-plugin](https://github.com/awc/emas-services-plugin))

## The difference between this plugin and the official gradle plugin

**This plugin supports multiple product flavors**

| \                  | Single Product Flavor | Multiple Product Flavor |
|:-------------------|:----------------------|:-------------------------|
| The official plugin | ./app/aliyun-emas-services.json | [x] |
| This plugin        | ./app/aliyun-emas-services.json<br/>(or) ./app/src/main/aliyun-emas-services.json | ./app/src/${anyFlavor}/aliyun-emas-services.json |

**Note:** *The packageName in the json configuration file must be consistent with the applicationId of the current channel in the Android project, otherwise the compilation will fail*

```sh
# Searched Location:
${TestProjectRootDir}/app/src/testFlavor/aliyun-emas-services.json
${TestProjectRootDir}/app/src/release/aliyun-emas-services.json
${TestProjectRootDir}/app/src/testFlavorRelease/aliyun-emas-services.json
${TestProjectRootDir}/app/src/testFlavor/release/aliyun-emas-services.json
${TestProjectRootDir}/app/src/release/testFlavor/aliyun-emas-services.json
${TestProjectRootDir}/app/aliyun-emas-services.json
```

## aliyun-emas-services.json

> support com.ali.ams:alicloud-android-third-push:3.2.0

| config.services | Artifact     | Latest version | The Official plugin | This plugin |
|:----------------|:-------------|:---------------|:-------------------|:------------|
| hotfix_service  | alicloud-android-hotfix | 3.2.15 |  [√] | [√] |
| ha-adapter_service  | alicloud-android-ha-adapter | 1.1.3.4-open |  [√] | [√] |
| feedback_service  | alicloud-android-feedback | 3.3.1 |  [√] | [√] |
| tlog_service  | alicloud-android-tlog | 1.1.2.3-open |  [√] | [√] |
| httpdns_service  | alicloud-android-httpdns | 1.3.2.3 |  [√] | [√] |
| apm_service  | alicloud-android-apm | 1.0.7.9-open |  [√] | [√] |
| man_service  | alicloud-android-man | 1.2.4 |  [√] | [√] |
| cps_service  | alicloud-android-push | 3.2.1 |  [√] | [√] |
| **third-cps_service**  | alicloud-android-third-push | 3.2.0 |  [×] | [√] |

## Usage

* Build the jar for this plugin

```sh
$ ./gradlew assemble
...
$ find . -name "emas-services*.jar"
./build/libs/emas-services-1.0.1.nvlab-SNAPSHOT.jar
```
* Using the plugin

```sh
$ mkdir ${THE_TARGET_PROJECT_PATH_USING_THE_PLUGIN}/plugins
$ cp ${THE_LOCAL_PATH_TO_THIS_REPO}/build/libs/emas-services-1.0.1.nvlab-SNAPSHOT.jar ${THE_TARGET_PROJECT_PATH_USING_THE_PLUGIN}/plugins/
```

For ${THE_TARGET_PROJECT_PATH_USING_THE_PLUGIN}/build.gradle

```groovy
buildscript {
    dependencies {
        classpath fileTree(include: ['*.jar'], dir: 'plugins')
        classpath files('plugins/emas-services-1.0.1.nvlab-SNAPSHOT.jar')
        // ...
    }
    // ...
}
```

For modules using the plugin(e.g. ${THE_TARGET_PROJECT_PATH_USING_THE_PLUGIN}/app/build.gradle)

```groovy
apply plugin: 'com.aliyun.ams.emas-services'
```


**Sample output:**

```
EMAS: configured services: {"hotfix_service":{"status":0,"version":"3.2.15"},"ha-adapter_service":{"status":0,"version":"1.1.3.4-open"},"feedback_service":{"status":0,"version":"3.3.1"},"tlog_service":{"status":0,"version":"1.1.2.3-open"},"httpdns_service":{"status":0,"version":"1.3.2.3"},"apm_service":{"status":0,"version":"1.0.7.9-open"},"man_service":{"status":0,"version":"1.2.4"},"cps_service":{"status":1,"version":"3.2.1"},"third-cps_service":{"status":1,"version":"3.2.0"}}
EMAS: hotfix_service service disabled!
EMAS: ha-adapter_service service disabled!
EMAS: feedback_service service disabled!
EMAS: tlog_service service disabled!
EMAS: httpdns_service service disabled!
EMAS: apm_service service disabled!
EMAS: man_service service disabled!
EMAS: add dependencies: testFlavorDebugImplementation com.aliyun.ams:alicloud-android-push:3.2.1
EMAS: add dependencies: testFlavorDebugImplementation com.aliyun.ams:alicloud-android-third-push:3.2.0
```

## [LICENSE](./LICENSE)
