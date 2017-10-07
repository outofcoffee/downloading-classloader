package com.gatehill.dlcl.classloader

import com.gatehill.dlcl.Collector
import com.gatehill.dlcl.Downloader
import com.gatehill.dlcl.mavenCentral
import com.gatehill.dlcl.model.DependencyType
import org.eclipse.aether.graph.Exclusion
import java.net.URI
import java.net.URLClassLoader

/**
 * A `ClassLoader` that downloads a dependency and its transitive dependencies
 * from Maven repositories, then makes them available on the classpath.
 *
 * @author pete
 */
open class DownloadingClassLoader(private val repoBaseDir: String,
                                  private val repositories: List<Pair<String, String>> = listOf(mavenCentral),
                                  parent: ClassLoader = getSystemClassLoader()) : URLClassLoader(emptyArray(), parent) {

    /**
     * Download the given dependencies, then load them into the `Classloader`.
     */
    fun load(vararg dependencyTypes: DependencyType = arrayOf(DependencyType.JAR)) {
        Collector(repoBaseDir).collectDependencies(*dependencyTypes)
                .forEach { addURL(it.file.toUri().toURL()) }
    }

    /**
     * Download the given dependencies, then load them into the `Classloader`.
     */
    fun fetchThenLoad(root: String, excludes: List<Exclusion> = emptyList(),
                      vararg dependencyTypes: DependencyType = arrayOf(DependencyType.JAR)) {

        Downloader(repoBaseDir, root, excludes, repositories).download()
        load(*dependencyTypes)
    }

    /**
     * Download the given file, then load it into the `Classloader`.
     */
    fun fetchFileThenLoad(uri: URI, vararg dependencyTypes: DependencyType = arrayOf(DependencyType.JAR)) {
        Downloader(repoBaseDir).downloadFile(uri)
        load(*dependencyTypes)
    }
}
