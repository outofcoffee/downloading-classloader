package com.gatehill.dlcl

import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.`should not be empty`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File
import java.nio.file.Paths

/**
 * Specification for `Downloader`.
 */
object DownloaderSpec : Spek({
    given("a downloader") {
        val downloader = Downloader(repoDir, dependency, excludes, repos)

        on("clearing repo") {
            File(repoDir).takeIf { it.exists() }?.deleteRecursively()

            it("should clear the repo") {
                Paths.get(repoDir).toFile().exists().`should be false`()
            }
        }

        on("downloading dependencies") {
            downloader.download()

            val jars = Collector(repoDir).collectDependencies()
            jars.forEach { println("Found: $it") }

            it("should return a list of JARs") {
                jars.`should not be empty`()
            }

            it("should have downloaded valid JARs") {
                jars.forEach { it.file.toFile().length() `should be greater than` 0 }
            }
        }
    }
})
