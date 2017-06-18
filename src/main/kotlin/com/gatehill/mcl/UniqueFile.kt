package com.gatehill.mcl

import java.nio.file.Path

class UniqueFile(val file: Path,
                 val hash: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        return hash == (other as? UniqueFile)?.hash
    }

    override fun hashCode() = hash.hashCode()

    override fun toString() = "UniqueFile(file=${file.fileName}, hash=$hash)"
}
