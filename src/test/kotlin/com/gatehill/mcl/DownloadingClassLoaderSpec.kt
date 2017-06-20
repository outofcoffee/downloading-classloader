package com.gatehill.mcl

import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be null`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `DownloadingClassLoader`.
 */
object DownloadingClassLoaderSpec : Spek({
    given("a class loader") {
        val classLoader = DownloadingClassLoader(repoDir, dependency, excludes, repos)

        on("creating class loader") {
            it("should create the class loader") {
                classLoader.`should not be null`()
            }

            val clazz = classLoader.loadClass(className)
            it("should load the class") {
                clazz.`should not be null`()
                clazz.canonicalName `should be` className
            }

            it("should instantiate the class") {
                val dataStore = clazz.newInstance()
                dataStore::class.java.canonicalName `should be` className
            }
        }
    }
})
