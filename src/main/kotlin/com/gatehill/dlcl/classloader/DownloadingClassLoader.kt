package com.gatehill.dlcl.classloader

import com.gatehill.dlcl.Downloader
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

    fun load(root: String, excludes: List<Exclusion> = emptyList()) {
        withDownloader(root, excludes) {
            collectJars().forEach { addURL(it.file.toUri().toURL()) }
        }
    }

    fun fetch(root: String, excludes: List<Exclusion> = emptyList()) {
        withDownloader(root, excludes) {
            download()
            collectJars().forEach { addURL(it.file.toUri().toURL()) }
        }
    }

    private fun withDownloader(root: String, excludes: List<Exclusion>, block: Downloader.() -> Unit) {
        with(Downloader(repoBaseDir, root, excludes, repositories)) { block() }
    }
}
