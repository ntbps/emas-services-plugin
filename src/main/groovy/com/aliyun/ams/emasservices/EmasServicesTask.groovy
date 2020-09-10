package com.aliyun.ams.emasservices


import com.aliyun.ams.emasservices.parser.EmasConfigParserUtils
import com.google.common.base.Charsets
import com.google.common.io.Files
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class EmasServicesTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(EmasServicesTask.class.getSimpleName())
    @Input
    public File sourceIntermediateDir

    @OutputDirectory
    public File resIntermediateDir

    @Input
    public String packageName

    @Input
    public String variantDir

    @TaskAction
    void action() throws IOException {
        JsonObject rootObject = null
        Project project = getProject()
        try {
            logger.info("Trying to parse the resources if the config file exists, app:${packageName}, variant:${variantDir}")
            rootObject = EmasDependencyWorker.getEmasServicesJson(project, variantDir)
        } catch (EmasServicesException e) {
            logger.error("${e.getMessage()}")
        }
        assertRecreateDir(resIntermediateDir)
        assertRecreateDir(sourceIntermediateDir)
        handleConfig(rootObject)
        handleProguardKeptList(rootObject)
    }

    private void handleConfig(JsonObject rootObject) throws IOException {
        if (rootObject == null) {

        }
        Map<String, Map<String, String>> resAttributes = new TreeMap<>()
        Map<String, String> resValues = EmasConfigParserUtils.getConfigResources(rootObject)
        File values = new File(resIntermediateDir, "ams_values")
        if (!values.exists() && !values.mkdirs())
            throw new EmasServicesException("Failed to create folder: " + values)
        String valuesContent = mapContentToXml(resValues, resAttributes)
        logger.info("valuesContent: ${valuesContent}")
        Files.write(valuesContent, new File(values, "ams_values.xml"), Charsets.UTF_8)
    }

    private static String mapContentToXml(Map<String, String> values, Map<String, Map<String, String>> attributes) {
        StringBuilder sb = new StringBuilder(256)
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n")
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String name = entry.key
            sb.append("    <string name=\"${name}\"")
            if (attributes.containsKey(name)) {
                for (Map.Entry<String, String> attr : attributes.get(name).entrySet()) {
                    sb.append(" ${attr.key}=\"${attr.value}\"")
                }
            }
            sb.append(">${entry.value}</string>\n")
        }
        sb.append("</resources>\n")
        return sb.toString()
    }

    private void handleProguardKeptList(JsonObject rootObject) throws IOException {
        JsonPrimitive proguardKeepList = rootObject.getAsJsonPrimitive("proguard_keeplist")
        if (proguardKeepList == null)
            return
        File sourceFile = new File(sourceIntermediateDir, "ams_values")
        if (!sourceFile.exists() && !sourceFile.mkdirs())
            throw new EmasServicesException("Failed to create folder: " + sourceFile)
        Files.write(proguardKeepList.getAsString(), new File(sourceFile, "ams_proguard_rules.pro"), Charsets.UTF_8)
    }

    private static void deleteFolder(File folder) {
        if (!folder.exists())
            return
        File[] files = folder.listFiles()
        if (files != null)
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file)
                } else if (!file.delete()) {
                    throw new EmasServicesException("Failed to delete: " + file)
                }
            }
        if (!folder.delete())
            throw new EmasServicesException("Failed to delete: " + folder)
    }

    private static void assertRecreateDir(File dir) {
        if (dir != null && dir.path != "") {
            deleteFolder(dir)
            if (!dir.mkdirs())
                throw new EmasServicesException("Failed to create folder: ${dir}")
        }
    }
}
