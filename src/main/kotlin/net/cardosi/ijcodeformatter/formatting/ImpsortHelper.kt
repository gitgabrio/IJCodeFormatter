
package net.cardosi.ijcodeformatter.formatting

import net.cardosi.ijcodeformatter.IJCodeFormatterException
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.twdata.maven.mojoexecutor.MojoExecutor

/**
 * Function responsible of Impsort invocation
 */
private const val NET_REVELC_CODE = "net.revelc.code"
private const val IMPSORT_MAVEN_PLUGIN = "impsort-maven-plugin"
private const val IMPSORT_GOAL = "sort"

@Throws(IJCodeFormatterException::class)
fun fixImports(
    mavenProject: MavenProject?,
    mavenSession: MavenSession?,
    pluginManager: BuildPluginManager?,
    configurationElements: Array<MojoExecutor.Element?>,
    impsortPluginVersion: String?,
    log: Log
) {
    log.info("Invoking $IMPSORT_MAVEN_PLUGIN")
    MojoExecutor.executeMojo(
        MojoExecutor.plugin(
            MojoExecutor.groupId(NET_REVELC_CODE),
            MojoExecutor.artifactId(IMPSORT_MAVEN_PLUGIN),
            MojoExecutor.version(impsortPluginVersion)
        ),
        MojoExecutor.goal(IMPSORT_GOAL), MojoExecutor.configuration(*configurationElements),
        MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager)
    )
}