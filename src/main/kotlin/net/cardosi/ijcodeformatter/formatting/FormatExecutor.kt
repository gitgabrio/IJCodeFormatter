
package net.cardosi.ijcodeformatter.formatting

import net.cardosi.ijcodeformatter.*
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.MojoExecution
import org.apache.maven.plugin.PluginParameterExpressionEvaluator
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.logging.SystemStreamLog
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.configuration.PlexusConfiguration
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration
import org.twdata.maven.mojoexecutor.MojoExecutor
import java.io.File
import java.nio.charset.StandardCharsets

class FormatExecutor  {
    private val log: Log = SystemStreamLog()

    // Formatter-plugin properties
    /**
     * ResourceManager for retrieving the configFile resource.
     */
    // @Component(role = ResourceManager.class)
//    private val resourceManager: ResourceManager? = null

    /**
     * Project's target directory as specified in the POM.
     */
    // @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private val targetDirectory: File? = null

    /**
     * Project's base directory as specified in the POM.
     */
    // @Parameter(defaultValue = "${project.basedir}", property = "baseDirectory", readonly = true, required = true)
    private val basedir: File? = null

    /**
     * Projects cache directory.
     *
     *
     *
     * This file is a hash cache of the files in the project source. It can be preserved in source code such that it
     * ensures builds are always fast by not unnecessarily writing files constantly. It can also be added to gitignore
     * in case startup is not necessary. It further can be redirected to another location.
     *
     *
     *
     * When stored in the repository, the cache if run on cross platforms will display the files multiple times due to
     * line ending differences on the platform.
     *
     *
     *
     * The cache itself has been part of formatter plugin for a long time but was hidden in target directory and did not
     * survive clean phase when it should. This is not intended to be clean in that way as one would want as close to a
     * no-op as possible when files are already all formatted and/or have not been otherwise touched. This is used based
     * off the files in the project so it is as much part of the source as any other file is.
     *
     *
     *
     * The cache can become invalid for any number of reasons that this plugin can't reasonably detect automatically. If
     * you rely on the cache and make any changes to the project that could conceivably make the cache invalid, or if
     * you notice that files aren't being reformatted when they should, just delete the cache and it will be rebuilt.
     *
     * @since 2.12.1
     */
    // @Parameter(defaultValue = "${project.build.directory}", property = "formatter.cachedir")
    private val cachedir: File? = null

    /**
     * Java compiler source version.
     */
    // @Parameter(defaultValue = "1.8", property = "maven.compiler.source", required = true)
    private val compilerSource: String? = null

    /**
     * Java compiler compliance version.
     */
    // @Parameter(defaultValue = "1.8", property = "maven.compiler.source", required = true)
    private val compilerCompliance: String? = null

    /**
     * Java compiler target version.
     */
    // @Parameter(defaultValue = "1.8", property = "maven.compiler.target", required = true)
    private val compilerTargetPlatform: String? = null

    /**
     * The file encoding used to read and write source files. When not specified and sourceEncoding also not set,
     * default is platform file encoding.
     *
     * @since 0.3
     */
    // @Parameter(property = "project.build.sourceEncoding", required = true)
    private val encoding: String? = null

    /**
     * File or classpath location of an Eclipse code formatter configuration xml file to use in formatting.
     */
    // @Parameter(defaultValue = "formatter-maven-plugin/eclipse/java.xml", property = "configfile", required = true)
    private val configFile: String? = null

    /**
     * File or classpath location of an Eclipse code formatter configuration xml file to use in formatting.
     */
    // @Parameter(defaultValue = "formatter-maven-plugin/eclipse/javascript.xml", property = "configjsfile", required = true)
    private val configJsFile: String? = null

    /**
     * File or classpath location of a properties file to use in html formatting.
     */
    // @Parameter(defaultValue = "formatter-maven-plugin/jsoup/html.properties", property = "confightmlfile", required = true)
    private val configHtmlFile: String? = null

    /**
     * File or classpath location of a properties file to use in xml formatting.
     */
    // @Parameter(defaultValue = "formatter-maven-plugin/eclipse/xml.properties", property = "configxmlfile", required = true)
    private val configXmlFile: String? = null

    /**
     * File or classpath location of a properties file to use in json formatting.
     */
    // @Parameter(defaultValue = "formatter-maven-plugin/jackson/json.properties", property = "configjsonfile", required = true)
    private val configJsonFile: String? = null

    /**
     * File or classpath location of a properties file to use in css formatting.
     */
    // @Parameter(defaultValue = "formatter-maven-plugin/ph-css/css.properties", property = "configcssfile", required = true)
    private val configCssFile: String? = null

    /**
     * Whether the java formatting is skipped.
     */
    // @Parameter(defaultValue = "false", property = "formatter.java.skip")
    private val skipJavaFormatting = false

    /**
     * Whether the javascript formatting is skipped.
     */
    // @Parameter(defaultValue = "false", property = "formatter.js.skip")
    private val skipJsFormatting = false

    /**
     * Whether the html formatting is skipped.
     */
    // @Parameter(defaultValue = "false", property = "formatter.html.skip")
    private val skipHtmlFormatting = false

    /**
     * Whether the xml formatting is skipped.
     */
    // @Parameter(defaultValue = "false", property = "formatter.xml.skip")
    private val skipXmlFormatting = false

    /**
     * Whether the json formatting is skipped.
     */
    // @Parameter(defaultValue = "false", property = "formatter.json.skip")
    private val skipJsonFormatting = false

    /**
     * Whether the css formatting is skipped.
     */
    // @Parameter(defaultValue = "false", property = "formatter.css.skip")
    private val skipCssFormatting = false

    /**
     * Whether the formatting is skipped.
     *
     * @since 0.5
     */
    // @Parameter(defaultValue = "false", alias = "skip", property = "formatter.skip")
    private val skipFormatting = false

    /**
     * Use eclipse defaults when set to true for java and javascript.
     */
    // @Parameter(defaultValue = "false", property = "formatter.useEclipseDefaults")
    private val useEclipseDefaults = false

    /**
     * A java regular expression pattern that can be used to exclude some portions of the java code from being
     * reformatted.
     *
     *
     * This can be useful when using DSL that embeds some kind of semantic hierarchy, where users can use various
     * indentation level to increase the readability of the code. Those semantics are ignored by the formatter, so this
     * regex pattern can be used to match certain portions of the code so that they will not be reformatted.
     *
     *
     * An example is the Apache Camel java DSL which can be used in the following way: ``<pre>
     * from("seda:a").routeId("a")
     * .log("routing at ${routeId}")
     * .multicast()
     * .to("seda:b")
     * .to("seda:c")
     * .end()
     * .log("End of routing");
    </pre> *  In the above example, the exercept can be skipped by the formatter by defining the following
     * property in the formatter xml configuration: `
     * <javaExclusionPattern>\b(from\([^;]*\.end[^;]*?\)\));</javaExclusionPattern>
    ` *
     *
     * @since 2.13
     */
    // @Parameter(property = "formatter.java.exclusion_pattern")
    private val javaExclusionPattern: String? = null

    // Impsort-plugin properties
    // @Parameter(defaultValue = "${project}", readonly = true)
    protected var project: MavenProject? = null

    // @Parameter(defaultValue = "${plugin}", readonly = true)
    protected var plugin: PluginDescriptor? = null

    // @Parameter(defaultValue = "${project.build.sourceEncoding}", readonly = true)
    protected var sourceEncoding = StandardCharsets.UTF_8.name()

    /**
     * Allows skipping execution of this plugin.
     *
     * @since 1.0.0
     */
    // @Parameter(alias = "skip", property = "impsort.skip", defaultValue = "false")
    private val skip = false

    /**
     * Configures the grouping of static imports. Groups are defined with comma-separated package name
     * prefixes. The special "*" group refers to imports not matching any other group, and is implied
     * after all other groups, if not specified. More specific groups are prioritized over less
     * specific ones. All groups are sorted.
     *
     * @since 1.0.0
     */
    // @Parameter(alias = "staticGroups", property = "impsort.staticGroups", defaultValue = "*")
    protected var staticGroups: String? = null

    /**
     * Configures the grouping of non-static imports. Groups are defined with comma-separated package
     * name prefixes. The special "*" group refers to imports not matching any other group, and is
     * implied after all other groups, if not specified. More specific groups are prioritized over
     * less specific ones. All groups are sorted.
     *
     * @since 1.0.0
     */
    // @Parameter(alias = "groups", property = "impsort.groups", defaultValue = "*")
    protected var groups: String? = null

    /**
     * Configures whether static groups will appear after non-static groups.
     *
     * @since 1.0.0
     */
    // @Parameter(alias = "staticAfter", property = "impsort.staticAfter", defaultValue = "false")
    protected var staticAfter = false

    /**
     * Allows omitting the blank line between the static and non-static sections.
     *
     * @since 1.0.0
     */
    // @Parameter(alias = "joinStaticWithNonStatic", property = "impsort.joinStaticWithNonStatic", defaultValue = "false")
    protected var joinStaticWithNonStatic = false

    /**
     * Configures whether to remove unused imports.
     *
     * @since 1.1.0
     */
    // @Parameter(alias = "removeUnused", property = "impsort.removeUnused", defaultValue = "false")
    private val removeUnused = false

    /**
     * Configures whether to treat imports in the current package as unused and subject to removal
     * along with other unused imports.
     *
     * @since 1.2.0
     */
    // @Parameter(alias = "treatSamePackageAsUnused", property = "impsort.treatSamePackageAsUnused",  defaultValue = "true")
    private val treatSamePackageAsUnused = false

    /**
     * Configures whether to use a breadth first comparator for sorting static imports. This will
     * ensure all static imports from one class are grouped together before any static imports from an
     * inner-class.
     *
     * @since 1.3.0
     */
    // @Parameter(alias = "breadthFirstComparator", property = "impsort.breadthFirstComparator", defaultValue = "true")
    private val breadthFirstComparator = false

    /**
     * Sets the Java source compliance level (e.g. 1.0, 1.5, 1.7, 8, 9, 11, etc.)
     *
     * @since 1.5.0
     */
    // @Parameter(alias = "compliance", property = "impsort.compliance", defaultValue = "${maven.compiler.release}")
    private val compliance: String? = null
    // Common properties
    /**
     * Project's main source directory as specified in the POM. Used by default if
     * `directories` is not set.
     *
     * @since 1.0.0
     */
    // @Parameter(alias = "sourceDirectory", defaultValue = "${project.build.sourceDirectory}",  readonly = true)
    private val sourceDirectory: File? = null

    /**
     * Project's test source directory as specified in the POM. Used by default if
     * `directories` is not set.
     *
     * @since 1.0.0
     */
    // @Parameter(alias = "testSourceDirectory", defaultValue = "${project.build.testSourceDirectory}",  readonly = true)
    private val testSourceDirectory: File? = null

    /**
     * Location of the Java source files to process. Defaults to source main and test directories if
     * not set.
     *
     * @since 1.0.0
     */
    // @Parameter(alias = "directories", property = "directories")
    private val directories: Array<File> = TODO()

    /**
     * List of fileset patterns for Java source locations to include. Patterns are relative to the
     * directories selected. When not specified, the default include is `**&#47;*.java`
     *
     * @since 1.0.0
     */
    // @Parameter(alias = "includes", property = "includes")
    private val includes: Array<String>

    /**
     * List of fileset patterns for Java source locations to exclude. Patterns are relative to the
     * directories selected. When not specified, there is no default exclude.
     *
     * @since 1.0.0
     */
    // @Parameter(alias = "excludes", property = "excludes")
    private val excludes: Array<String>
    /**
     * Sets the line-ending of files after formatting. Valid values are:
     *
     *  * **"AUTO"** - Use line endings of current system
     *  * **"KEEP"** - Preserve line endings of files, default to AUTO if ambiguous
     *  * **"LF"** - Use Unix and Mac style line endings
     *  * **"CRLF"** - Use DOS and Windows style line endings
     *  * **"CR"** - Use early Mac style line endings
     *
     *
     * @since 0.2.0
     */
    // @Parameter(defaultValue = "AUTO", property = "lineending", required = true)
    //private LineEnding lineEnding;
    // Plugin versions
    /**
     * The `maven-scm-plugin` version to use.
     * Default to **1.12.0**
     */
    // @Parameter(defaultValue = "1.12.0", property = "scmPluginVersion")
    private val scmPluginVersion: String? = null

    /**
     * The `formatter-maven-plugin` version to use.
     * Default to **2.16.0**
     */
    // @Parameter(defaultValue = "2.16.0", property = "formatterPluginVersion")
    private val formatterPluginVersion: String? = null

    /**
     * The `impsort-maven-plugin` version to use.
     * Default to **1.5.0**
     */
    // @Parameter(defaultValue = "1.5.0", property = "impsortPluginVersion")
    private val impsortPluginVersion: String? = null

    // FormatMojo properties
    // @Component
    private val mavenProject: MavenProject = getMavenProject(File("pom.xml"))

    // @Component
    private val mavenSession: MavenSession = getMavenSession(mavenProject)

    // @Component
    private val pluginManager: BuildPluginManager? = null

    // @Parameter(defaultValue = "${mojoExecution}")
    protected var mojoExecution: MojoExecution = getMojoExecution()

    /**
     * Execute.
     *
     * @throws IJCodeFormatterException
     */
    @Throws(IJCodeFormatterException::class)
    fun execute() {
        log.info("Begin execution....")
        val files = getModifiedFiles(
            mavenProject,
            mavenSession,
            pluginManager,
            scmPluginVersion,
            log
        )
        try {
            val pomConfiguration: PlexusConfiguration = XmlPlexusConfiguration(mojoExecution.configuration)
            val expressionEvaluator = PluginParameterExpressionEvaluator(mavenSession, mojoExecution)
            val includes: MojoExecutor.Element = getIncludesElement(files, log)
            val formatterConfigurationElements: Array<MojoExecutor.Element?> = getFormatterConfigurationElements(
                includes,
                pomConfiguration,
                expressionEvaluator,
                log
            )
            formatFiles(
                mavenProject,
                mavenSession,
                pluginManager,
                formatterConfigurationElements,
                formatterPluginVersion,
                log
            )
            val impsortConfigurationElements: Array<MojoExecutor.Element?> = getImpsortConfigurationElements(
                includes,
                pomConfiguration,
                expressionEvaluator,
                log
            )
            fixImports(
                mavenProject,
                mavenSession,
                pluginManager,
                impsortConfigurationElements,
                impsortPluginVersion,
                log
            )
        } catch (e: Exception) {
            log.error(e)
            throw IJCodeFormatterException(e.message)
        }
        log.info("....done!")
    }
}