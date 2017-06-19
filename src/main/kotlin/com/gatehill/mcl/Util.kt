package com.gatehill.mcl

import org.eclipse.aether.graph.Exclusion

val mavenCentral = "central" to "https://repo.maven.apache.org/maven2/"
val jcenter = "jcenter" to "https://jcenter.bintray.com/"
val jitpack = "jitpack" to "https://jitpack.io"

fun exclusion(groupId: String, artifactId: String) = Exclusion(groupId, artifactId, null, null)
