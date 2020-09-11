package com.aliyun.ams.emasservices

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class EmasDependencyAnalyzer {
    private static final Logger logger = Logging.getLogger(EmasDependencyAnalyzer.class.getSimpleName())

    EmasDependencyAnalyzer(String projectName) {
        highlightOutput("EMAS: Created $this for project:${projectName}")
        highlightOutput("EMAS: Please note that if the gradle task fails, please execute \"./gradle \${task} --debug\" in the terminal for more detailed information")
    }

    static void handleDependenciesServices(Project project, def variant) throws IOException {
        String variantDir = variant.dirName
        JsonObject rootObject = EmasDependencyWorker.getEmasServicesJson(project, variantDir)
        if (rootObject == null) {
            return
        }
        JsonPrimitive whetherUseMavenObject = rootObject.getAsJsonPrimitive("use_maven")
        if (whetherUseMavenObject == null)
            throw new EmasServicesException("Missing useMavenDependencies so can not add dependencies!")
        JsonObject servicesObject = rootObject.getAsJsonObject("services")
        if (servicesObject == null) {
            throw new EmasServicesException("Missing services so can not add dependencies!")
        }
        boolean useMaven = whetherUseMavenObject.asBoolean
        if (useMaven) {
            logger.info("emas:use_maven = true, so will add maven dependency!")
        } else {
            logger.info("emas:use_maven = false, so will add libs/ aar/jar as dependency!")
        }
        String flavor = variant.flavorName
        String configuration
        if (flavor == null || flavor == "") {
            configuration = "implementation" // "compile"
        } else {
            String buildType = "${variant.name}".replace("${variant.flavorName}", "")
            configuration = "${flavor}${buildType}Implementation"
            project.configurations {
                "${flavor}${buildType}Implementation" {}
            }
        }
        boolean hasServices = false
        for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>) servicesObject.entrySet()) {
            // a = a || b(). if a = true, b won't be invoked
            hasServices = checkServiceDependency(project, configuration, entry.key, entry.value, useMaven) || hasServices
        }
        if (hasServices && !useMaven) {
            ConfigurableFileTree configurableFileTree = project.fileTree(new HashMap<String, String>(2))
            highlightOutput("EMAS: add dependencies: ${configuration} ${configurableFileTree}")
            project.dependencies.add(configuration, configurableFileTree)
            project.repositories.flatDir(new HashMap<String, String>(1))
        }
    }

    private static boolean checkServiceDependency(Project project, String configuration, String name, JsonElement config, boolean useMaven) {
        String artifactId = EmasDependencyWorker.getArtifactWithServiceName(name)
        logger.info("EMAS:parse service: ${name} -> ${artifactId}")
        if (artifactId != null) {
            ServiceEntity service = (ServiceEntity) (new Gson()).fromJson(config, ServiceEntity.class)
            if (service == null) {
                return false
            }
            switch (service.status) {
                case EmasDependencyWorker.SERVICE_ENABLED:
                    if (useMaven) {
                        String artifact = "${EmasDependencyWorker.MODULE_GROUP}:${artifactId}:${service.version}"
                        highlightOutput("EMAS: ${name} enabled, add dependencies: ${configuration} ${artifact}")
                        project.dependencies.add(configuration, artifact)
                        return true
                    } else {
                        final String fileName = "${artifactId}-${service.version}"
                        File file = new File("${project.projectDir.path}/libs/${fileName}.aar")
                        if (file.exists()) {
                            highlightOutput("EMAS: ${name} enabled, add local sdk dependency: ${configuration} ${fileName}")
                            project.dependencies.add(configuration, new HashMap<String, String>())
                            return true
                        } else {
                            throw new EmasServicesException("use_maven=false means use local sdk,but can't find :${file.name}, please download sdk in emas console!")
                        }
                    }
                case EmasDependencyWorker.SERVICE_DISABLED:
                    highlightOutput("EMAS: ${name} service disabled!")
                    break
                default:
                    highlightOutput("EMAS: ${name} service unknow status!")
                    break
            }
        }
        return false
    }

    static highlightOutput(String message) {
        logger.warn("\033[032m\033[1m${message}\033[0m\033[0m")
    }
}
