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
    private static final String CONFIGURATION = "implementation" // "compile"

    EmasDependencyAnalyzer(String projectName) {
        logger.info("Created $this for project:${projectName}")
        logger.info("Please note that if the gradle task fails, please execute \"./gradle \${task} --debug\" in the terminal for more detailed information")
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
        boolean hasServices = false
        for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>) servicesObject.entrySet()) {
            hasServices = handleDependenciesService(project, entry, hasServices, useMaven)
        }
        if (hasServices && !useMaven) {
            ConfigurableFileTree configurableFileTree = project.fileTree(new HashMap<String, String>(2))
            project.dependencies.add(CONFIGURATION, configurableFileTree)
            project.repositories.flatDir(new HashMap<String, String>(1))
        }
    }

    private static boolean handleDependenciesService(Project project,
                                                     Map.Entry<String, JsonElement> entry,
                                                     boolean hasServices,
                                                     boolean useMaven) {
        String artifactId = EmasDependencyWorker.getArtifactWithServiceName(entry.key)
        if (artifactId != null) {
            logger.info("emas:parse service : " + (String) entry.key)
            ServiceEntity serviceEntity = (ServiceEntity) (new Gson()).fromJson(entry.value, ServiceEntity.class)
            if (serviceEntity != null) {
                switch (serviceEntity.status) {
                    case EmasDependencyWorker.SERVICE_ENABLED:
                        hasServices = true
                        if (useMaven) {
                            String artifact = "${EmasDependencyWorker.MODULE_GROUP}:${artifactId}:${serviceEntity.version}"
                            logger.info("add dependencies :${artifact}")
                            project.dependencies.add(CONFIGURATION, artifact)
                            return hasServices
                        }
                        final String fileName = artifactId + "-" + serviceEntity.version
                        File file = new File("${project.projectDir.path}/libs/${fileName}.aar")
                        if (file.exists()) {
                            logger.info("add local sdk dependency : ${fileName}")
                            project.dependencies.add(CONFIGURATION, new HashMap<String, String>())
                            return hasServices
                        } else {
                            throw new EmasServicesException("use_maven=false means use local sdk,but can't find :${file.name}, please download sdk in emas console!")
                        }
                        break
                    case EmasDependencyWorker.SERVICE_DISABLED:
                        logger.warn("${entry.key as String} service disabled!")
                        return hasServices
                    default:
                        logger.warn("${entry.key as String} service unknow status!")
                        break
                }
            }
        }
        return hasServices
    }
}
