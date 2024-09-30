package at.xirado.bean.util

import at.xirado.bean.Module
import java.io.InputStream
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory

fun getResourcesRoot(): Path {
    val url = Module::class.java.getResource("/")
        ?: throw IllegalStateException("Could not determine root of resources directory")

    return when (url.protocol) {
        "file" -> Paths.get(url.toURI())
        "jar" -> {
            val uri = URI.create(url.toString().substringBefore("!"))
            FileSystems.newFileSystem(uri, emptyMap<String, Any>()).use { fs ->
                fs.getPath("/")
            }
        }
        else -> throw IllegalStateException("Unsupported resource protocol ${url.protocol}")
    }
}

fun listFilesInResources(resourcePath: String = "/"): List<String> {
    // Get the URL for the resource path
    val resourceUrl = Module::class.java.getResource(resourcePath)
        ?: throw IllegalArgumentException("Directory $resourcePath not found")

    return when (resourceUrl.protocol) {
        "file" -> {
            // Handle resources in the file system
            val uri = resourceUrl.toURI()
            val dirPath = Paths.get(uri)

            Files.list(dirPath).use { stream ->
                stream.map { it.fileName.toString() }.toList()
            }
        }

        "jar" -> {
            // Handle resources inside a JAR
            val uri = URI.create(resourceUrl.toString().substringBefore("!"))
            FileSystems.newFileSystem(uri, emptyMap<String, Any>()).use { fs ->
                val dirPath = fs.getPath(resourcePath)
                Files.list(dirPath).use { stream ->
                    stream.map { it.fileName.toString() }.toList()
                }
            }
        }

        else -> throw UnsupportedOperationException("Unsupported protocol: ${resourceUrl.protocol}")
    }
}

fun <T> getResource(path: String, use: InputStream.() -> T): T? {
    val resource = Module::class.java.getResourceAsStream(path)
        ?: return null

    return resource.use(use)
}

fun <T> getResourceFilesRecursively(
    path: String,
    filter: Path.() -> Boolean = { true },
    map: (Path) -> T,
): List<T> {
    val resourceUrl = Module::class.java.getResource(path)
        ?: throw IllegalArgumentException("Directory $path not found")

    return when (resourceUrl.protocol) {
        "file" -> {
            // Handle resources in the file system
            val uri = resourceUrl.toURI()
            val dirPath = Paths.get(uri)
            Files.walk(dirPath).use {
                it
                    .filter { !it.isDirectory() && filter(it) }
                    .map(map)
                    .toList()
            }
        }

        "jar" -> {
            // Handle resources inside a JAR
            val uri = URI.create(resourceUrl.toString().substringBefore("!"))
            FileSystems.newFileSystem(uri, emptyMap<String, Any>()).use { fs ->
                val dirPath = fs.getPath(path)
                Files.walk(dirPath).use {
                    it
                        .filter { !it.isDirectory() && filter(it) }
                        .map(map)
                        .toList()
                }
            }
        }

        else -> throw UnsupportedOperationException("Unsupported protocol: ${resourceUrl.protocol}")
    }
}