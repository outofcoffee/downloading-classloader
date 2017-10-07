package com.gatehill.dlcl

const val jarDependencyCoordinates = "com.gatehill.corebot:stores-redis:0.9.0-SNAPSHOT"

const val jarDependencyClassName = "com.gatehill.corebot.store.redis.RedisDataStoreImpl"

const val repoDir = "target/local-repo"

const val warDependencyCoordinates = "org.eclipse.jetty:test-jetty-webapp:war:9.4.7.v20170914"

const val warDependencyUrl = "http://repo1.maven.org/maven2/org/eclipse/jetty/test-jetty-webapp/9.4.7.v20170914/test-jetty-webapp-9.4.7.v20170914.war"

const val warDependencyFilename = "test-jetty-webapp-9.4.7.v20170914.war"

const val warDependencyNestedJarClassName = "org.eclipse.jetty.http.HttpFields"

val excludes = listOf(exclusion("org.jetbrains.kotlin", "kotlin-stdlib"))

val repos = listOf(
        mavenCentral,
        jcenter,
        jitpack,
        "gatehill" to "https://gatehillsoftware-maven.s3.amazonaws.com/snapshots/"
)
