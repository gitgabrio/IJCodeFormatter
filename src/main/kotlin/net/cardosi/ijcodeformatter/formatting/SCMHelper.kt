
package net.cardosi.ijcodeformatter

import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.twdata.maven.mojoexecutor.MojoExecutor
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*

/**
 * Function responsible of SCM invocation
 */
private const val ORG_APACHE_MAVEN_PLUGINS = "org.apache.maven.plugins"
private const val SCM_PLUGIN = "maven-scm-plugin"
private const val CONNECTIONTYPE_CONFIG = "connectionType"
private const val DEVELOPERCONNECTION = "developerConnection"
private const val DIFF_GOAL = "diff"
private val FILE_IDENTIFIERS = arrayOf("--- a/", "diff --git a/")
private val TO_REMOVE = arrayOf(
    String.format("src%1\$smain%1\$sjava%1\$s", File.separatorChar),
    String.format("src%1\$smain%1\$sresources%1\$s", File.separatorChar),
    String.format("src%1\$stest%1\$sjava%1\$s", File.separatorChar),
    String.format("src%1\$stest%1\$sresources%1\$s", File.separatorChar)
)

@Throws(IJCodeFormatterException::class)
fun getModifiedFiles(
    mavenProject: MavenProject,
    mavenSession: MavenSession?,
    pluginManager: BuildPluginManager?,
    scmPluginVersion: String?,
    log: Log
): List<File> {
    log.info("Invoking $SCM_PLUGIN")
    MojoExecutor.executeMojo(
        MojoExecutor.plugin(
            MojoExecutor.groupId(ORG_APACHE_MAVEN_PLUGINS),
            MojoExecutor.artifactId(SCM_PLUGIN),
            MojoExecutor.version(scmPluginVersion)
        ),
        MojoExecutor.goal(DIFF_GOAL), MojoExecutor.configuration(
            MojoExecutor.element(
                MojoExecutor.name(
                    CONNECTIONTYPE_CONFIG
                ), DEVELOPERCONNECTION
            )
        ),
        MojoExecutor.executionEnvironment(mavenSession, pluginManager)
    )
    val diffFileName = mavenProject.artifactId + ".diff"
    val diffFile = File(diffFileName)
    return ArrayList(getModifiedFiles(diffFile, log))
}

private fun getModifiedFiles(
    diffFile: File,
    log: Log
): Set<File> {
    log.info("Reading file $diffFile")
    val toReturn: MutableSet<File> = HashSet()
    try {
        Files.lines(diffFile.toPath(), StandardCharsets.UTF_8)
            .forEach { s: String ->
                parseLine(s, log)
                    .ifPresent { e: File -> toReturn.add(e) }
            }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return toReturn
}

private fun parseLine(
    line: String,
    log: Log
): Optional<File> {
    log.debug("Parsing line $line")
    for (fileIndentifier in FILE_IDENTIFIERS) {
        if (line.startsWith(fileIndentifier)) {
            val toParse = line.replace(fileIndentifier, "")
            var filePart = toParse.split(" ").toTypedArray()[0]
            for (toRemove in TO_REMOVE) {
                filePart = filePart.replace(toRemove, "")
            }
            return Optional.of(File(filePart))
        }
    }
    return Optional.empty()
}