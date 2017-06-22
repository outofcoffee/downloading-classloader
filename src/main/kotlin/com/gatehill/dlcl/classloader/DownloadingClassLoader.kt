package com.gatehill.dlcl.classloader

import com.gatehill.dlcl.mavenCentral
import org.eclipse.aether.graph.Exclusion
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

    fun fetch(root: String, excludes: List<Exclusion> = emptyList()) {
        with(com.gatehill.dlcl.Downloader(repoBaseDir, root, excludes, repositories)) {
            download()
            collectJars().forEach { addURL(it.file.toUri().toURL()) }
        }
    }
}
