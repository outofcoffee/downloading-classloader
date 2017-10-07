package com.gatehill.dlcl

import com.gatehill.dlcl.model.DependencyType
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be empty`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.ActionBody
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File
import java.net.URI
import java.nio.file.Paths

/**
 * Specification for `Downloader`.
 */
object DownloaderSpec : Spek({
    given("a downloader") {
        val downloader = Downloader(repoDir, jarDependencyCoordinates, excludes, repos)

        on("clearing repo") {
            File(repoDir).takeIf { it.exists() }?.deleteRecursively()

            it("clears the repo") {
                Paths.get(repoDir).toFile().exists().`should be false`()
            }
        }

        on("downloading JAR dependencies") {
            downloader.download()

            val jars = Collector(repoDir).collectDependencies()
            jars.forEach { println("Found: $it") }

            it("returns a list of JARs") {
                jars.`should not be empty`()
            }

            it("downloaded valid JARs") {
                jars.forEach { it.file.toFile().length() `should be greater than` 0 }
            }
        }

        on("downloading a WAR file by coordinates") {
            val collector = Collector(repoDir)
            collector.clearCollected()

            downloader.downloadSingleDependency(warDependencyCoordinates)
            verifyDownloadedWar(collector)
        }

        on("downloading a WAR file by URL") {
            val collector = Collector(repoDir)
            collector.clearCollected()

            downloader.downloadFile(URI.create(warDependencyUrl))
            verifyDownloadedWar(collector)
        }
    }
})

private fun ActionBody.verifyDownloadedWar(collector: Collector) {
    it("downloaded the WAR and extracted it") {
        val dependencyFileName = Paths.get(repoDir, warDependencyFilename)
        dependencyFileName.toFile().exists().`should be false`()

        val outputDir = Paths.get(dependencyFileName.toString()
                .substring(0, dependencyFileName.toString().lastIndexOf(".")))

        outputDir.toFile().exists().`should be true`()
    }

    it("can read the nested JARs") {
        val nestedJars = collector.collectDependencies(DependencyType.JAR)
        nestedJars.count() `should equal` 7

        nestedJars[0].file.toFile().length() `should be greater than` 0
    }
}
