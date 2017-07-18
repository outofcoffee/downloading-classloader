package com.gatehill.dlcl

import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should not be empty`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File
import java.nio.file.Paths

/**
 * Specification for `Collector`.
 */
object CollectorSpec : Spek({
    given("a collector") {
        val collector = Collector(repoDir)

        on("clearing repo") {
            collector.clearCollected()

            it("should clear the repo") {
                Paths.get(repoDir).toFile().exists().`should be false`()
            }
        }

        on("collecting dependencies") {
            File(repoDir).mkdirs()
            File(repoDir, "example1.jar").createNewFile()
            File(repoDir, "example2.jar").createNewFile()

            val jars = collector.collectJars()
            jars.forEach { println("Found: $it") }

            it("should return a list of JARs") {
                jars.`should not be empty`()
            }

            it("should have collected existent JAR files") {
                jars.forEach { it.file.toFile().exists().`should be true`() }
            }
        }
    }
})
