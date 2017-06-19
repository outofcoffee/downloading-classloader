package com.gatehill.mcl

import org.eclipse.aether.graph.Exclusion
import java.net.URLClassLoader

/**
 * A `ClassLoader` that downloads a dependency and its transitive dependencies
 * from Maven repositories, then makes them available on the classpath.
 *
 * @author pete
 */
class MavenClassLoader(repoBaseDir: String,
                       root: String,
                       excludes: List<Exclusion> = emptyList(),
                       repositories: List<Pair<String, String>> = listOf(mavenCentral)) : URLClassLoader(emptyArray()) {

    init {
        with(Downloader(repoBaseDir, root, excludes, repositories)) {
            download()
            collectJars().forEach { addURL(it.file.toUri().toURL()) }
        }
    }
}
