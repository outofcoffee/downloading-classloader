Example:

```
const val dependency = "com.gatehill.corebot:stores-redis:0.9.0-SNAPSHOT"
const val className = "com.gatehill.corebot.store.redis.RedisDataStoreImpl"

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
```
