package com.aliyun.ams.emasservices

import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

@Deprecated
class EmasDependencyInspector implements DependencyResolutionListener {
    private static final Logger logger = Logging.getLogger(EmasDependencyInspector.class.getSimpleName())
    private final EmasDependencyAnalyzer analyzer
    private final String projectName
    private final String exceptionMessageAddendum

    EmasDependencyInspector(EmasDependencyAnalyzer analyzer, String projectName, String exceptionMessageAddendum) {
        logger.info("Created $this for project:${projectName}")
        this.analyzer = analyzer
        this.projectName = projectName
        this.exceptionMessageAddendum = exceptionMessageAddendum
    }

    @Override
    void beforeResolve(ResolvableDependencies deps) {
        String module = getModuleName(deps)
        logger.info("DependencyInspector.beforeResolve: modole:${module}, name:${deps.name}, path:${deps.path}")
        try {
            logger.info("files: ${deps.getFiles()}")
        } catch (Exception e) {
            logger.error("Failed to list files: ${e.getMessage()}")
        }
    }

    @Override
    void afterResolve(ResolvableDependencies deps) {
        String module = getModuleName(deps)
        logger.info("DependencyInspector.afterResolve: module:${module}, name:${deps.name}, path:${deps.path}")
    }

    private static String getModuleName(ResolvableDependencies deps) {
        try {
            return deps.path.replace(":${deps.name}", "").substring(1)
        } catch (Exception e) {
            logger.error("Failed to parse module name: ${e.getMessage()}")
            return ""
        }
    }
}