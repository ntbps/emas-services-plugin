package com.aliyun.ams.emasservices

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.Plugin;
import org.gradle.api.Project;

class EmasServicesPlugin implements Plugin<Project>, GroovyObject {

    private static final Logger logger = Logging.getLogger(EmasServicesPlugin.class.getSimpleName())

    @Override
    void apply(Project project) {
        EmasDependencyAnalyzer globalDependencies = new EmasDependencyAnalyzer(project.name)
//        project.afterEvaluate {
//            logger.info("EmasServicePlugin: project.afterEvaluate")
//            project.getGradle().addListener(new EmasDependencyInspector(
//                    globalDependencies,
//                    project.name,
//                    "This error message came from the emas-services Gradle plugin, report" +
//                            " issues at http://github.com/awc/emas-services-plugin and disable by " +
//                            "adding \"emasServices { disableVersionCheck = true }\" to your build.gradle file."))
//        }

        for (PluginType pluginType : PluginType.values()) {
            for (String plugin : pluginType.plugins()) {
                if (project.plugins.hasPlugin(plugin)) {
                    setupPlugin(project, globalDependencies, pluginType)
                    return
                }
            }
        }

        // If the emas-service plugin is applied before any android plugin.
        // We should warn that emas service plugin should be applied at
        // the bottom of build file.
        showWarningForPluginLocation(project)

        // Setup emas-services plugin after android plugin is applied.
        project.plugins.withId("android", {
            setupPlugin(project, globalDependencies, PluginType.APPLICATION)
        })
        project.plugins.withId("android-library", {
            setupPlugin(project, globalDependencies, PluginType.LIBRARY)
        })
        project.plugins.withId("android-feature", {
            setupPlugin(project, globalDependencies, PluginType.FEATURE)
        })
    }

    private static void handleVariant(Project project, EmasDependencyAnalyzer analyzer, def variant) {
        logger.info("handleVariant: name:${variant.name}, dir:${variant.dirName}, pkg:${variant.applicationId}, flavor:${variant.flavorName}, build:${variant.buildType.name}")
        try {
            EmasDependencyWorker.checkIfApplicationIdMatched(project, variant)
        } catch (EmasServicesException e) {
            logger.debug("${variant.applicationId}:${variant.flavorName}: ${e.getMessage()}")
            return
        }

        String dirName = variant.dirName
        File resOutputDir = project.file("$project.buildDir/generated/res/emas-services/$dirName")
        File sourceOutputDir = project.file("$project.buildDir/generated/source/emas-services/$dirName")
        EmasServicesTask task = project.tasks.create("process${variant.name.capitalize()}EmasServices", EmasServicesTask)
        task.resIntermediateDir = resOutputDir
        task.sourceIntermediateDir = sourceOutputDir
        task.variantDir = dirName
        task.packageName = variant.applicationId

        // This is necessary for backwards compatibility with versions of gradle that do not support
        // this new API.
        if (variant.respondsTo("registerGeneratedResFolders")) {
            task.ext.generatedResFolders = project.files(resOutputDir).builtBy(task)
            variant.registerGeneratedResFolders(task.generatedResFolders)
            if (variant.respondsTo("getMergeResourcesProvider")) {
                variant.mergeResourcesProvider.configure { dependsOn(task) }
            } else {
                // noinspection GrDeprecatedAPIUsage
                variant.mergeResources.dependsOn(task)
            }
        } else {
            // noinspection GrDeprecatedAPIUsage
            variant.registerResGeneratingTask(task, resOutputDir)
        }

        try {
            analyzer.handleDependenciesServices(project, variant)
        } catch (EmasServicesException e) {
            logger.error("${e.getMessage()}")
        }
    }

    private static void showWarningForPluginLocation(Project project) {
        logger.warn("Please apply emas-services plugin at the top of the build file.");
    }

    private static void setupPlugin(Project project, EmasDependencyAnalyzer analyzer, PluginType pluginType) {
        switch (pluginType) {
            case PluginType.APPLICATION:
                project.android.applicationVariants.all { variant ->
                    handleVariant(project, analyzer, variant)
                }
                break
            case PluginType.LIBRARY:
                project.android.libraryVariants.all { variant ->
                    handleVariant(project, analyzer, variant)
                }
                break
            case PluginType.FEATURE:
                project.android.featureVariants.all { variant ->
                    handleVariant(project, analyzer, variant)
                }
                break
            case PluginType.MODEL_APPLICATION:
                project.model.android.applicationVariants.all { variant ->
                    handleVariant(project, analyzer, variant)
                }
                break
            case PluginType.MODEL_LIBRARY:
                project.model.android.libraryVariants.all { variant ->
                    handleVariant(project, analyzer, variant)
                }
                break
        }
    }

    // These are the plugin types and the set of associated plugins whose presence should be checked for.
    private final static enum PluginType {
        APPLICATION([
                "android",
                "com.android.application"
        ]),
        LIBRARY([
                "android-library",
                "com.android.library"
        ]),
        FEATURE([
                "android-feature",
                "com.android.feature"
        ]),
        MODEL_APPLICATION([
                "com.android.model.application"
        ]),
        MODEL_LIBRARY(["com.android.model.library"])

        PluginType(Collection plugins) {
            this.plugins = plugins
        }
        private final Collection plugins

        Collection plugins() {
            return plugins
        }
    }
}
