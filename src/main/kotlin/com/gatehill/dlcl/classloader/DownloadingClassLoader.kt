package com.gatehill.dlcl.classloader

/**
 * A `ClassLoader` that downloads a dependency and its transitive dependencies
 * from Maven repositories, then makes them available on the classpath.
 *
 * @author pete
 */
open class DownloadingClassLoader(repoBaseDir: String,
                                  root: String,
                                  excludes: List<org.eclipse.aether.graph.Exclusion> = emptyList(),
                                  repositories: List<Pair<String, String>> = listOf(com.gatehill.dlcl.mavenCentral),
                                  parent: ClassLoader = getSystemClassLoader()) : java.net.URLClassLoader(emptyArray(), parent) {

    init {
        with(com.gatehill.dlcl.Downloader(repoBaseDir, root, excludes, repositories)) {
            download()
            collectJars().forEach { addURL(it.file.toUri().toURL()) }
        }
    }
}
