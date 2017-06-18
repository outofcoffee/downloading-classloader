import com.gatehill.plugins.Downloader
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be empty`
import org.amshove.kluent.`should not be null`
import org.eclipse.aether.artifact.DefaultArtifact
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.api.dsl.xon
import java.net.URLClassLoader
import java.nio.file.Paths

const val dependency = "com.gatehill.corebot:stores-redis:0.9.0-SNAPSHOT"
const val className = "com.gatehill.corebot.store.redis.RedisDataStoreImpl"
const val repoDir = "target/local-repo"
val excludes = listOf(DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:0"))

/**
 * Specification for `Downloader`.
 */
object DownloaderSpec : Spek({
    given("a downloader") {
        val downloader = Downloader(repoDir, dependency, excludes)

        xon("clearing repo") {
            downloader.clearRepo()

            it("should clear the repo") {
                Paths.get(repoDir).toFile().exists().`should be false`()
            }
        }

        on("downloading dependencies") {
            downloader.download()

            val jars = downloader.collectJars()
            jars.forEach { println("Found: $it") }

            it("should have downloaded JARs") {
                jars.`should not be empty`()
            }

            it("should have downloaded valid JARs") {
                jars.forEach { it.file.toFile().length() `should be greater than` 0 }
            }
        }

        on("creating classloader") {
            val classLoader = URLClassLoader(downloader.collectJars().map { it.file.toUri().toURL() }.toTypedArray())

            it("should create the classloader") {
                classLoader.`should not be null`()
            }

            val clazz = classLoader.loadClass(className)
            it("should load the class") {
                clazz.`should not be null`()
                clazz.canonicalName `should be` className
            }

            it("should instantiate the class") {
                val dataStore = clazz.newInstance()
                dataStore::class.java.canonicalName `should be` className
            }
        }
    }
})
