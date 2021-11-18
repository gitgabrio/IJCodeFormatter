
package net.cardosi.ijcodeformatter.formatting

import org.apache.commons.lang3.ObjectUtils
import org.apache.commons.lang3.StringUtils
import org.apache.maven.plugin.PluginParameterExpressionEvaluator
import org.apache.maven.plugin.logging.Log
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException
import org.codehaus.plexus.configuration.PlexusConfiguration
import org.twdata.maven.mojoexecutor.MojoExecutor
import java.io.File
import java.util.*
import java.util.stream.Collectors

private const val INCLUDES = "includes"
private val FORMATTER_PARAMETERS = listOf(
    "sourceDirectory",
    "testSourceDirectory",
    "targetDirectory",
    "basedir",
    "cachedir",
    "directories",
    INCLUDES,
    "excludes",
    "compilerSource",
    "compilerCompliance",
    "compilerTargetPlatform",
    "encoding",
    "lineEnding",
    "configFile",
    "configJsFile",
    "configHtmlFile",
    "configXmlFile",
    "configJsonFile",
    "configCssFile",
    "skipJavaFormatting",
    "skipJsFormatting",
    "skipHtmlFormatting",
    "skipXmlFormatting",
    "skipJsonFormatting",
    "skipCssFormatting",
    "skipFormatting",
    "useEclipseDefaults",
    "javaExclusionPattern"
)
private val IMPSORT_PARAMETERS = listOf(
    "sourceEncoding",
    "skip",
    "staticGroups",
    "groups",
    "staticAfter",
    "joinStaticWithNonStatic",
    "sourceDirectory",
    "testSourceDirectory",
    "directories",
    "includes",
    "excludes",
    "removeUnused",
    "treatSamePackageAsUnused",
    "breadthFirstComparator",
    "lineEnding",
    "compliance"
)

@Throws(ExpressionEvaluationException::class)
fun getFormatterConfigurationElements(
    includes: MojoExecutor.Element,
    pomConfiguration: PlexusConfiguration,
    expressionEvaluator: PluginParameterExpressionEvaluator,
    log: Log
): Array<MojoExecutor.Element?> {
    log.info("Executing getFormatterConfigurationElements")
    val plexusConfigurations = getPlexusConfigurations(pomConfiguration, FORMATTER_PARAMETERS, log)
    return getConfigurationElements(includes, plexusConfigurations, expressionEvaluator, log)
}

@Throws(ExpressionEvaluationException::class)
fun getImpsortConfigurationElements(
    includes: MojoExecutor.Element,
    pomConfiguration: PlexusConfiguration,
    expressionEvaluator: PluginParameterExpressionEvaluator,
    log: Log
): Array<MojoExecutor.Element?> {
    log.info("Executing getImpsortConfigurationElements")
    val plexusConfigurations = getPlexusConfigurations(pomConfiguration, IMPSORT_PARAMETERS, log)
    return getConfigurationElements(includes, plexusConfigurations, expressionEvaluator, log)
}

@Throws(ExpressionEvaluationException::class)
private fun getConfigurationElements(
    includes: MojoExecutor.Element,
    plexusConfigurations: List<PlexusConfiguration>,
    expressionEvaluator: PluginParameterExpressionEvaluator,
    log: Log
): Array<MojoExecutor.Element?> {
    log.info("Executing getConfigurationElements")
    val toReturn = arrayOfNulls<MojoExecutor.Element>(plexusConfigurations.size)
    for (i in toReturn.indices) {
        val plexusConfiguration = plexusConfigurations[i]
        if (plexusConfiguration.name == INCLUDES) {
            toReturn[i] = includes
        } else {
            toReturn[i] = getElement(plexusConfiguration, expressionEvaluator, log)
        }
    }
    return toReturn
}


fun getIncludesElement(
    files: List<File>,
    log: Log
): MojoExecutor.Element {
    log.debug("getIncludesElement $files")
    val children = arrayOfNulls<MojoExecutor.Element>(files.size)
    for (i in children.indices) {
        children[i] = MojoExecutor.Element("", files[i].toString())
    }
    return MojoExecutor.element(MojoExecutor.name(INCLUDES), *children)
}

@Throws(ExpressionEvaluationException::class)
private fun getElement(
    plexusConfiguration: PlexusConfiguration,
    expressionEvaluator: PluginParameterExpressionEvaluator,
    log: Log
): MojoExecutor.Element {
    log.debug("getElement $plexusConfiguration")
    val value = plexusConfiguration.value
    val defaultValue = plexusConfiguration.getAttribute("default-value")
    val configurationName = plexusConfiguration.name
    val evaluated =
        ObjectUtils.defaultIfNull(expressionEvaluator.evaluate(StringUtils.defaultIfBlank(value, defaultValue)), "")
            .toString()
    return MojoExecutor.element(MojoExecutor.name(configurationName), evaluated)
}

private fun getPlexusConfigurations(
    pomConfiguration: PlexusConfiguration,
    parameters: List<String>,
    log: Log
): List<PlexusConfiguration> {
    log.debug("getPlexusConfigurations")
    return Arrays.stream(pomConfiguration.children)
        .filter { plexusConfiguration: PlexusConfiguration -> parameters.contains(plexusConfiguration.name) }
        .collect(Collectors.toList())
}
