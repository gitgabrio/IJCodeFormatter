
package net.cardosi.ijcodeformatter

import org.apache.maven.execution.*
import org.apache.maven.model.Model
import org.apache.maven.model.Plugin
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.plugin.MojoExecution
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.DefaultPlexusContainer
import org.codehaus.plexus.PlexusContainer
import java.io.File
import java.io.FileReader

fun getMavenProject(pomFile: File?): MavenProject {
    var model: Model? = null
    var reader: FileReader? = null
    val mavenreader = MavenXpp3Reader()
    try {
        reader = FileReader(pomFile)
        model = mavenreader.read(reader)
        model.pomFile = pomFile
    } catch (ex: Exception) {
    }
    return MavenProject(model)
}

fun getMavenSession(project: MavenProject): MavenSession {
    val container: PlexusContainer = DefaultPlexusContainer()
    val request: MavenExecutionRequest = DefaultMavenExecutionRequest()
    val result: MavenExecutionResult = DefaultMavenExecutionResult()
    result.project = project
    return MavenSession(container, request, result, project)
}

fun getMojoExecution(): MojoExecution {
    return MojoExecution(Plugin(), "format", "format")
}