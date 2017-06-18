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
import org.eclipse.aether.util.filter.ExclusionsDependencyFilter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.function.BiPredicate
import java.util.stream.Collectors
import javax.xml.bind.DatatypeConverter

class Downloader(private val repoBaseDir: String,
                 private val root: String,
                 private val excludes: List<Artifact>) {

    val system = newRepositorySystem()
    val session = newRepositorySystemSession(system, repoBaseDir)
    val artifactCache = mutableListOf<Artifact>()

    fun download(coordinates: String = root, scope: String = JavaScopes.RUNTIME) =
            download(DefaultArtifact(coordinates), scope)

    fun download(artifact: Artifact, scope: String = JavaScopes.RUNTIME) {
        if (artifactCache.contains(artifact)) {
            println("Already downloaded: $artifact")
            return
        } else {
            artifactCache += artifact
        }
        println("Downloading: $artifact")

        val classpathFilter = DependencyFilterUtils.andFilter(
                DependencyFilterUtils.classpathFilter(scope),
                ExclusionsDependencyFilter(excludes.map { "${it.groupId}:${it.artifactId}" })
        )

        val collectRequest = CollectRequest()
        collectRequest.root = Dependency(artifact, scope)
        collectRequest.repositories = newRepositories()

        val dependencyRequest = DependencyRequest(collectRequest, classpathFilter)
        val dependencyResult = system.resolveDependencies(session, dependencyRequest)

        dependencyResult.artifactResults.forEach {
            println("Resolved: ${it.artifact} to: ${it.artifact.file}")
        }

        dependencyResult.root.children
                .filterNot { child -> excludes.any { child.artifact.groupId == it.groupId && child.artifact.artifactId == it.artifactId } }
                .filter { scope == it.dependency.scope }
                .forEach { child -> download(child.artifact) }
    }

    private fun newRepositorySystem(): RepositorySystem {
        val locator = DefaultServiceLocator()
        locator.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
        locator.addService(VersionResolver::class.java, DefaultVersionResolver::class.java)
        locator.addService(VersionRangeResolver::class.java, DefaultVersionRangeResolver::class.java)
        locator.addService(ArtifactDescriptorReader::class.java, DefaultArtifactDescriptorReader::class.java)
        locator.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)
        locator.addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
        return locator.getService(RepositorySystem::class.java)
    }

    private fun newRepositorySystemSession(system: RepositorySystem, repoBaseDir: String): DefaultRepositorySystemSession {
        val session = MavenRepositorySystemUtils.newSession()

        val localRepo = LocalRepository(repoBaseDir)
        session.localRepositoryManager = system.newLocalRepositoryManager(session, localRepo)

        return session
    }

    private fun newRepositories(): List<RemoteRepository> = listOf(
            RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build(),
            RemoteRepository.Builder("jcenter", "default", "https://jcenter.bintray.com/").build(),
            RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build(),
            RemoteRepository.Builder("gatehill", "default", "https://gatehillsoftware-maven.s3.amazonaws.com/snapshots/").build()
    )

    fun collectJars(): List<UniqueFile> {
        println("Collecting JARs")

        return Files
                .find(Paths.get(repoBaseDir), 10, BiPredicate { path, _ -> path.fileName.toString().endsWith(".jar") })
                .parallel()
                .map { it.toAbsolutePath() }
                .map { UniqueFile(it, checksum(it)) }
                .distinct()
                .collect(Collectors.toList())
    }

    private fun checksum(file: Path): String {
        Files.newInputStream(file).use { stream ->
            val digest = MessageDigest.getInstance("MD5")
            val block = ByteArray(4096)

            do {
                val length = stream.read(block)
                if (length <= 0) break
                digest.update(block, 0, length)
            } while (true)

            return DatatypeConverter.printHexBinary(digest.digest())
        }
    }

    class UniqueFile(val file: Path,
                     val hash: String) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false
            other as UniqueFile
            return (hash == other.hash)
        }

        override fun hashCode() = hash.hashCode()

        override fun toString() = "UniqueFile(file=${file.fileName}, hash=$hash)"
    }
}

fun main(args: Array<String>) {
    Paths.get("target/local-repo").toFile()
            .takeIf { it.exists() }
            ?.deleteRecursively()

    // TODO generate excludes based on resolved dependencies for `core-api` and `core-engine`
    val excludes = listOf(
            DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:0"),
            DefaultArtifact("org.jetbrains.kotlin:kotlin-reflect:0"),
            DefaultArtifact("org.jetbrains:annotations:0"),
            DefaultArtifact("javax.inject:javax.inject:0"),
            DefaultArtifact("org.apache.logging.log4j:log4j-api:0"),
            DefaultArtifact("com.google.inject:guice:0"),
            DefaultArtifact("com.google.guava:guava:0"),
            DefaultArtifact("com.fasterxml.jackson.module:jackson-module-kotlin:0"),
            DefaultArtifact("com.fasterxml.jackson.core:jackson-databind:0"),
            DefaultArtifact("com.fasterxml.jackson.core:jackson-annotations:0"),
            DefaultArtifact("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:0"),
            DefaultArtifact("com.fasterxml.jackson.core:jackson-core:0"),
            DefaultArtifact("org.yaml:snakeyaml:0"),
            DefaultArtifact("aopalliance:aopalliance:0"),
            DefaultArtifact("com.gatehill.corebot:core-api:0"),
            DefaultArtifact("com.gatehill.corebot:core-engine:0")
    )

    with(Downloader("target/local-repo", "com.gatehill.corebot:stores-redis:0.9.0-SNAPSHOT", excludes)) {
        download()

        val jars = collectJars()
        jars.forEach { println("Found: $it") }
    }
}
