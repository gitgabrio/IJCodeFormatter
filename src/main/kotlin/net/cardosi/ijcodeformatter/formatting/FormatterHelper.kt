
package net.cardosi.ijcodeformatter.formatting

import net.cardosi.ijcodeformatter.IJCodeFormatterException
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.twdata.maven.mojoexecutor.MojoExecutor

/**
 * Function responsible of Formatter invocation
 */

private const val NET_REVELC_CODE_FORMATTER = "net.revelc.code.formatter"
private const val FORMATTER_MAVEN_PLUGIN = "formatter-maven-plugin"
private const val FORMAT_GOAL = "format"

@Throws(IJCodeFormatterException::class)
fun formatFiles(
    mavenProject: MavenProject?,
    mavenSession: MavenSession?,
    pluginManager: BuildPluginManager?,
    configurationElements: Array<MojoExecutor.Element?>,
    formatterPluginVersion: String?,
    log: Log
) {
    log.info("Invoking $FORMATTER_MAVEN_PLUGIN")
    MojoExecutor.executeMojo(
        MojoExecutor.plugin(
            MojoExecutor.groupId(NET_REVELC_CODE_FORMATTER), MojoExecutor.artifactId(
                FORMATTER_MAVEN_PLUGIN
            ), MojoExecutor.version(formatterPluginVersion)
        ),
        MojoExecutor.goal(FORMAT_GOAL), MojoExecutor.configuration(*configurationElements),
        MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager)
    )
}