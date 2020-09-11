# emas-services-plugin

The Gradle plugin to help with using Aliyun EMAS services SDK(Support multiple product flavros).<br/>
([Main repo: https://github.com/awc/emas-services-plugin](https://github.com/awc/emas-services-plugin))

## The difference between this plugin and the official gradle plugin

**This plugin supports multiple product flavros**

| \                  | Single Product Flavor | Multiple Product Flavor |
|:-------------------|:----------------------|:-------------------------|
| The offical plugin | ./app/aliyun-emas-services.json | [x] |
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

## [LICENSE](./LICENSE)
