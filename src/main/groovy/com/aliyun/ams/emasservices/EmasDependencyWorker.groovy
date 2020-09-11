package com.aliyun.ams.emasservices

import com.aliyun.ams.emasservices.parser.EmasConfigParserUtils
import com.google.common.base.Charsets
import com.google.common.io.Files
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.util.function.Function
import java.util.function.IntPredicate
import java.util.regex.Matcher
import java.util.regex.Pattern

import static java.util.stream.Collectors.toList

class EmasDependencyWorker {
    private static final Logger logger = Logging.getLogger(EmasDependencyWorker.class.getSimpleName())
    public static final String JSON_FILE_NAME = "aliyun-emas-services.json"
    // Some example of things that match this pattern are:
    // "aBunchOfFlavors/release"
    // "flavor/debug"
    // "test"
    // And here is an example with the capture groups in [square brackets]
    // [a][BunchOfFlavors]/[release]
    public final static Pattern VARIANT_PATTERN = Pattern.compile("(?:([^\\p{javaUpperCase}]+)((?:\\p{javaUpperCase}[^\\p{javaUpperCase}]*)*)\\/)?([^\\/]*)")
    // Some example of things that match this pattern are:
    // "TestTheFlavor"
    // "FlavorsOfTheRainbow"
    // "Test"
    // And here is an example with the capture groups in [square brackets]
    // "[Flavors][Of][The][Rainbow]"
    // Note: Pattern must be applied in a loop, not just once.
    public final static Pattern FLAVOR_PATTERN = Pattern.compile("(\\p{javaUpperCase}[^\\p{javaUpperCase}]*)")

    public static final int SERVICE_ENABLED = 1

    public static final int SERVICE_DISABLED = 0

    public static final String SERVICE = "_service"

    public static final String CPS = "cps"
    public static final String CPS_THIRD_PART = "third-cps"

    public static final String PUSH = "push"

    public static final String PRE_ARTIFACT = "alicloud-android-"

    public static final String MODULE_GROUP = "com.aliyun.ams"

    static void checkIfApplicationIdMatched(Project project, def variant) {
        JsonObject root = getEmasServicesJson(project, (variant.dirName as String))
        String applicationId = (variant.applicationId as String)
        String packageName = EmasConfigParserUtils.getPackageName(root, applicationId)
        if (packageName != applicationId) {
            throw new EmasServicesException("Mismatched config/emas.package_name object. actual:${packageName}, desired:${applicationId}")
        }
    }

    static JsonObject getEmasServicesJson(Project project, String variantDir) {
        File quickStartFile = getQuickStartFile(project, variantDir)
        logger.warn("EMAS: Parsing json file: ${quickStartFile.path}")
        JsonElement root = (new JsonParser()).parse(Files.newReader(quickStartFile, Charsets.UTF_8))
        if (!root.isJsonObject())
            throw new EmasServicesException("Malformed root json")
        return root.asJsonObject
    }

    private static File getQuickStartFile(Project project, String variantDir) {
        File quickstartFile = null
        List<String> fileLocations = getJsonLocations(variantDir)
        String searchedLocation = System.lineSeparator()
        for (String location : fileLocations) {
            File jsonFile = project.file(location + '/' + JSON_FILE_NAME)
            searchedLocation = searchedLocation + jsonFile.path + System.lineSeparator()
            if (jsonFile.isFile()) {
                quickstartFile = jsonFile
                break
            }
        }

        if (quickstartFile == null || !quickstartFile.isFile()) {
            quickstartFile = project.file(JSON_FILE_NAME)
            searchedLocation = searchedLocation + quickstartFile.path
        }
        if (!quickstartFile.isFile()) {
            throw new EmasServicesException("File ${quickstartFile.name} is missing. The EMAS Services Plugin cannot function without it. \n Searched Location: ${searchedLocation}")
        }
        return quickstartFile
    }

    private static List<String> getJsonLocations(String variantDir) {
        logger.info("getJsonLocations: ${variantDir}")
        Matcher variantMatcher = VARIANT_PATTERN.matcher(variantDir)
        List<String> fileLocations = new ArrayList<>()
        if (!variantMatcher.matches()) {
            return fileLocations
        }
        List<String> flavorNames = new ArrayList<>()
        if (variantMatcher.group(1) != null) {
            flavorNames.add(variantMatcher.group(1).toLowerCase())
        }
        flavorNames.addAll(splitVariantNames(variantMatcher.group(2)))
        String buildType = variantMatcher.group(3)
        String flavorName = variantMatcher.group(1) + variantMatcher.group(2)
        fileLocations.add("src/" + flavorName + "/" + buildType)
        fileLocations.add("src/" + buildType + "/" + flavorName)
        fileLocations.add("src/" + flavorName)
        fileLocations.add("src/" + buildType)
        fileLocations.add("src/" + flavorName + capitalize(buildType))
        fileLocations.add("src/" + buildType)
        String fileLocation = "src"
        for (String flavor : flavorNames) {
            fileLocation += "/" + flavor
            fileLocations.add(fileLocation)
            fileLocations.add(fileLocation + "/" + buildType)
            fileLocations.add(fileLocation + capitalize(buildType))
        }

        fileLocations = fileLocations.stream().distinct().sorted(Comparator.comparing(new Function<String, String>() {
            @Override
            String apply(String s) {
                return countSlashes(s)
            }
        })).collect(toList())
        return fileLocations
    }

    static String getArtifactWithServiceName(String service_name) {
        if (service_name == null || service_name == "")
            return null
        if (service_name.endsWith(SERVICE))
            service_name = service_name.replace(SERVICE, "")
        if (service_name == CPS || service_name == CPS_THIRD_PART)
            service_name = service_name.replace(CPS, PUSH)
        return "${PRE_ARTIFACT}${service_name}"
    }

    private static String capitalize(String s) {
        if (s.length() == 0) return s
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()
    }

    private static long countSlashes(String input) {
        return input.codePoints().filter(new IntPredicate() {

            @Override
            boolean test(int value) {
                return (value as Character) == '/'
            }
        }).count()
    }

    private static List<String> splitVariantNames(String variant) {
        if (variant == null) {
            return new ArrayList<>()
        }
        List<String> flavors = new ArrayList<>()
        Matcher flavorMatcher = FLAVOR_PATTERN.matcher(variant)
        while (flavorMatcher.find()) {
            String match = flavorMatcher.group(1)
            if (match != null) {
                flavors.add(match.toLowerCase())
            }
        }
        return flavors
    }
}
