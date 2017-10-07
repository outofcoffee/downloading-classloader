package com.gatehill.dlcl.model

enum class DependencyType(val extension: String,
                          val nestedPath: String? = null) {
    JAR("jar"),
    WAR("war", "/WEB-INF/lib");

    val isContainer get() = nestedPath != null

    companion object {
        fun find(name: String): DependencyType? = DependencyType.values().firstOrNull {
            name.endsWith(".${it.extension}")
        }
    }
}
