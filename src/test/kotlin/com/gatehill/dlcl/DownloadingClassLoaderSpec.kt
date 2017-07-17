package com.gatehill.dlcl

import com.gatehill.dlcl.classloader.ChildFirstDownloadingClassLoader
import com.gatehill.dlcl.classloader.DownloadingClassLoader
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
    val parentClassLoader = DownloadingClassLoaderSpec::class.java.classLoader

    listOf(DownloadingClassLoader(repoDir, repos),
            ChildFirstDownloadingClassLoader(repoDir, repos, parentClassLoader)).forEach { classLoader ->

        given("a ${classLoader::class.java.simpleName}") {
            on("fetching dependency") {
                classLoader.fetchThenLoad(dependency, excludes)

                val clazz = classLoader.loadClass(className)
                it("can load the class") {
                    clazz.`should not be null`()
                    clazz.canonicalName `should be` className
                }

                it("can instantiate the class") {
                    val dataStore = clazz.newInstance()
                    dataStore::class.java.canonicalName `should be` className
                }
            }
        }
    }
})
