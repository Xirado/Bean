package at.xirado.bean.io.config

import at.xirado.bean.Application
import at.xirado.bean.io.exception.MissingPropertyException
import at.xirado.simplejson.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths

class FileLoader {
    companion object {

        fun loadResourceAsJson(fileName: String): JSONObject {
            val inputStream = Application::class.java.getResourceAsStream("/$fileName")?: throw FileNotFoundException("Resource $fileName not found")
            return JSONObject.fromJson(inputStream)
        }

        fun loadResourceAsYaml(fileName: String): JSONObject {
            val inputStream = Application::class.java.getResourceAsStream("/$fileName")?: throw FileNotFoundException("Resource $fileName not found")
            return JSONObject.fromYaml(inputStream)
        }

        fun loadFileAsJson(fileName: String, copyFromResources: Boolean): JSONObject {
            val file = File(fileName)
            check(fileName, file, copyFromResources)
            return JSONObject.fromJson(FileInputStream(file))
        }

        fun loadFileAsYaml(fileName: String, copyFromResources: Boolean): JSONObject {
            val file = File(fileName)
            check(fileName, file, copyFromResources)
            return JSONObject.fromYaml(FileInputStream(file))
        }

        private fun check(fileName: String, file: File, copyFromResources: Boolean) {
            if (!file.exists()) {
                if (!copyFromResources) {
                    throw FileNotFoundException("File $fileName was not found!")
                }

                val inputStream = Application::class.java.getResourceAsStream("/$fileName")
                    ?: throw FileNotFoundException("File $fileName was not found in resources folder!")

                val path = Paths.get(fileName)

                Files.copy(inputStream, path)
            }
        }

//        private fun getJarPath(): String {
//            val codeSource = Application::class.java.protectionDomain.codeSource
//            val jarFile = File(codeSource.location.toURI().path)
//            return jarFile.parentFile.path
//        }
    }
}

fun JSONObject.anyNull(vararg keys: String): Boolean {
    for (key in keys) {
        if (isNull(key)) {
            return true
        }
    }
    return false
}

fun JSONObject.nullOrBlank(key: String): Boolean {
    return isNull(key) || getString(key).isBlank()
}

fun JSONObject.getStringOrThrow(key: String): String {
    if (nullOrBlank(key))
        throw MissingPropertyException(property = key)

    return getString(key)
}