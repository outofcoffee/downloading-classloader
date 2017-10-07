package com.gatehill.dlcl

import com.gatehill.dlcl.classloader.ChildFirstDownloadingClassLoader
import com.gatehill.dlcl.classloader.DownloadingClassLoader
import com.gatehill.dlcl.model.DependencyType
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
            on("fetching JAR dependency") {
                classLoader.fetchThenLoad(jarDependencyCoordinates, excludes)

                val clazz = classLoader.loadClass(jarDependencyClassName)
                it("can load the class") {
                    clazz.`should not be null`()
                    clazz.canonicalName `should be` jarDependencyClassName
                }

                it("can instantiate the class") {
                    val dataStore = clazz.newInstance()
                    dataStore::class.java.canonicalName `should be` jarDependencyClassName
                }
            }

            on("fetching WAR dependency") {
                // clear then download
                Collector(repoDir).clearCollected()

                it("can fetch the dependencies") {
                    classLoader.fetchSingleDependencyThenLoad(warDependencyCoordinates, DependencyType.JAR)
                }

                it("can load and instantiate the class") {
                    val clazz = classLoader.loadClass(warDependencyNestedJarClassName)
                    clazz.`should not be null`()
                    clazz.canonicalName `should be` warDependencyNestedJarClassName

                    // instantiate
                    val httpFields = clazz.newInstance()
                    httpFields::class.java.canonicalName `should be` warDependencyNestedJarClassName
                }
            }
        }
    }
})
