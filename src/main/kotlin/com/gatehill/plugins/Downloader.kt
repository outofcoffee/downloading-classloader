package com.gatehill.plugins

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader
import org.apache.maven.repository.internal.DefaultVersionRangeResolver
import org.apache.maven.repository.internal.DefaultVersionResolver
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.impl.ArtifactDescriptorReader
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.impl.VersionRangeResolver
import org.eclipse.aether.impl.VersionResolver
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils

private val blacklist = listOf(
        "jdk"
)

class Downloader(repoBaseDir: String) {
    val system = newRepositorySystem()
    val session = newRepositorySystemSession(system, repoBaseDir)
    val artifactCache = mutableListOf<Artifact>()

    fun download(coordinates: String, scope: String = JavaScopes.COMPILE) {
        download(DefaultArtifact(coordinates), scope)
    }

    fun download(artifact: Artifact, scope: String = JavaScopes.COMPILE) {
        if (artifactCache.contains(artifact)) {
            println("Already downloaded: $artifact")
            return
        } else {
            artifactCache += artifact
        }
        println("Downloading: $artifact")

        val classpathFilter = DependencyFilterUtils.classpathFilter(scope)

        val collectRequest = CollectRequest()
        collectRequest.root = Dependency(artifact, scope)
        collectRequest.repositories = newRepositories()

        val dependencyRequest = DependencyRequest(collectRequest, classpathFilter)
        val dependencyResult = system.resolveDependencies(session, dependencyRequest)

        dependencyResult.artifactResults.forEach {
            println("${it.artifact} resolved to ${it.artifact.file}")
        }

        dependencyResult.root.children
                .filterNot { blacklist.contains(it.artifact.groupId) }
                .filter { arrayOf(JavaScopes.COMPILE, JavaScopes.RUNTIME).contains(it.dependency.scope) }
                .forEach { child -> download(child.artifact) }
    }

    fun newRepositorySystem(): RepositorySystem {
        val locator = DefaultServiceLocator()
        locator.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
        locator.addService(VersionResolver::class.java, DefaultVersionResolver::class.java)
        locator.addService(VersionRangeResolver::class.java, DefaultVersionRangeResolver::class.java)
        locator.addService(ArtifactDescriptorReader::class.java, DefaultArtifactDescriptorReader::class.java)
        locator.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)
        locator.addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
        return locator.getService(RepositorySystem::class.java)
    }

    fun newRepositorySystemSession(system: RepositorySystem, repoBaseDir: String): DefaultRepositorySystemSession {
        val session = MavenRepositorySystemUtils.newSession()

        val localRepo = LocalRepository(repoBaseDir)
        session.localRepositoryManager = system.newLocalRepositoryManager(session, localRepo)

        return session
    }

    fun newRepositories(): List<RemoteRepository> = listOf(
            RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build(),
            RemoteRepository.Builder("jcenter", "default", "https://jcenter.bintray.com/").build(),
            RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build(),
            RemoteRepository.Builder("gatehill", "default", "https://gatehillsoftware-maven.s3.amazonaws.com/snapshots/").build()
    )
}

fun main(args: Array<String>) {
    Downloader("target/local-repo").download("com.gatehill.corebot:backends-items:0.9.0-SNAPSHOT")
}
