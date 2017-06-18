package com.gatehill.mcl

import org.eclipse.aether.artifact.Artifact
import java.net.URLClassLoader

/**
 * A `ClassLoader` that downloads dependencies from Maven repositories,
 * then makes them available on the classpath.
 *
 * @author pete
 */
class MavenClassLoader(repoBaseDir: String,
                       root: String,
                       excludes: List<Artifact>,
                       repositories: List<Pair<String, String>>) : URLClassLoader(emptyArray()) {

    val downloader = Downloader(repoBaseDir, root, excludes, repositories)

    init {
        with(downloader) {
            download()
            collectJars().forEach { super.addURL(it.file.toUri().toURL()) }
        }
    }
}
