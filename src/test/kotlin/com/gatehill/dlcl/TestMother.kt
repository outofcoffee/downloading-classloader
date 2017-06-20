package com.gatehill.dlcl

const val dependency = "com.gatehill.corebot:stores-redis:0.9.0-SNAPSHOT"

const val className = "com.gatehill.corebot.store.redis.RedisDataStoreImpl"

const val repoDir = "target/local-repo"

val excludes = listOf(exclusion("org.jetbrains.kotlin", "kotlin-stdlib"))

val repos = listOf(
        mavenCentral,
        jcenter,
        jitpack,
        "gatehill" to "https://gatehillsoftware-maven.s3.amazonaws.com/snapshots/"
)
