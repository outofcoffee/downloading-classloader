package com.gatehill.mcl

import org.eclipse.aether.artifact.DefaultArtifact

const val dependency = "com.gatehill.corebot:stores-redis:0.9.0-SNAPSHOT"

const val className = "com.gatehill.corebot.store.redis.RedisDataStoreImpl"

const val repoDir = "target/local-repo"

val excludes = listOf(DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:0"))

val repos = listOf(
        mavenCentral,
        jcenter,
        jitpack,
        "gatehill" to "https://gatehillsoftware-maven.s3.amazonaws.com/snapshots/"
)
