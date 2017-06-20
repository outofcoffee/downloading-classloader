package com.gatehill.mcl

import org.eclipse.aether.graph.Exclusion
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.ArrayList
import java.util.Enumeration


/**
 * Applies child-first semantics to `DownloadingClassLoader`.
 *
 * @author pete
 */
class ChildFirstDownloadingClassLoader(repoBaseDir: String,
                                       root: String,
                                       excludes: List<Exclusion>,
                                       repositories: List<Pair<String, String>>,
                                       parent: ClassLoader) :
        DownloadingClassLoader(repoBaseDir, root, excludes, repositories, parent) {

    private val system = getSystemClassLoader()

    @Synchronized @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String, resolve: Boolean): Class<*>? {
        // First, check if the class has already been loaded
        var c: Class<*>? = findLoadedClass(name)
        if (c == null) {
            try {
                // checking local
                c = findClass(name)
            } catch (e: ClassNotFoundException) {
                c = loadClassFromParent(name, resolve)
            } catch (e: SecurityException) {
                c = loadClassFromParent(name, resolve)
            }

        }
        if (resolve)
            resolveClass(c)
        return c
    }

    @Throws(ClassNotFoundException::class)
    private fun loadClassFromParent(name: String, resolve: Boolean): Class<*>? {
        // checking parent
        // This call to loadClass may eventually call findClass
        // again, in case the parent doesn't find anything.
        var c: Class<*>?
        try {
            c = super.loadClass(name, resolve)
        } catch (e: ClassNotFoundException) {
            c = loadClassFromSystem(name)
        } catch (e: SecurityException) {
            c = loadClassFromSystem(name)
        }

        return c
    }

    @Throws(ClassNotFoundException::class)
    private fun loadClassFromSystem(name: String): Class<*>? {
        var c: Class<*>? = null
        if (system != null) {
            // checking system: jvm classes, endorsed, cmd classpath,
            // etc.
            c = system.loadClass(name)
        }
        return c
    }

    override fun getResource(name: String): URL? {
        var url: URL? = findResource(name)
        if (url == null)
            url = super.getResource(name)

        if (url == null && system != null)
            url = system.getResource(name)

        return url
    }

    @Throws(IOException::class)
    override fun getResources(name: String): Enumeration<URL> {
        /**
         * Similar to super, but local resources are enumerated before parent
         * resources
         */
        var systemUrls: Enumeration<URL>? = null
        if (system != null) {
            systemUrls = system.getResources(name)
        }
        val localUrls = findResources(name)
        var parentUrls: Enumeration<URL>? = null
        if (parent != null) {
            parentUrls = parent.getResources(name)
        }
        val urls = ArrayList<URL>()
        if (localUrls != null) {
            while (localUrls.hasMoreElements()) {
                val local = localUrls.nextElement()
                urls.add(local)
            }
        }
        if (systemUrls != null) {
            while (systemUrls.hasMoreElements()) {
                urls.add(systemUrls.nextElement())
            }
        }
        if (parentUrls != null) {
            while (parentUrls.hasMoreElements()) {
                urls.add(parentUrls.nextElement())
            }
        }
        return object : Enumeration<URL> {
            internal var iter = urls.iterator()

            override fun hasMoreElements(): Boolean {
                return iter.hasNext()
            }

            override fun nextElement(): URL {
                return iter.next()
            }
        }
    }

    override fun getResourceAsStream(name: String): InputStream? {
        try {
            return getResource(name)?.openStream()
        } catch (e: IOException) {
        }

        return null
    }
}
