package at.xirado.bean.data

import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Single
import java.io.File
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.io.path.isDirectory
import kotlin.io.path.toPath
import kotlin.streams.asSequence

private val log = KotlinLogging.logger { }

@Single
class ResourceService {
    private val resourcesRoot = ResourceService::class.java.getResource("/logback.xml")
        ?: throw IllegalStateException("Could not get logback.xml! (Why??)")
    private var fs: FileSystem? = null
    private val fsLock = ReentrantLock()
    private val isJar = isApplicationRunningInJar()
    private var useCount: Int = 0
    private var lastUse: Long = System.currentTimeMillis()
    private val nestedAccessCounter = ThreadLocal.withInitial { 0 }

    init {
        setupFileSystemClosureTask()
    }

    fun getResourcesRootPath(): Path {
        return accessFileSystem { fs ->
            val logbackPath = resourcesRoot.toURI().toPath()
            logbackPath.parent
        }
    }

    fun getFile(relativePath: Path): File {
        return accessFileSystem { fs ->
            val rootPath = getResourcesRootPath()
            val realPath = rootPath.resolve(relativePath)
            realPath.toFile()
        }
    }

    fun <T> getResourceFile(
        path: String,
        map: (Path) -> T,
    ): T = accessFileSystem { _ ->
        val rootPath = getResourcesRootPath()
        val realPath = rootPath.resolve(path)

        map(realPath.relativizeToResourcesRoot())
    }

    fun <T> getResourceFilesRecursively(
        path: String,
        filter: Path.() -> Boolean = { true },
        map: (Path) -> T?,
    ): List<T> = accessFileSystem { _ ->
        val rootPath = getResourcesRootPath()
        val realPath = rootPath.resolve(path)
        Files.walk(realPath).use { stream ->
            stream.asSequence()
                .filter { !it.isDirectory() && filter(it.relativizeToResourcesRoot()) }
                .mapNotNull { map(it.relativizeToResourcesRoot()) }
                .toList()
        }
    }

    private fun Path.relativizeToResourcesRoot(): Path = getResourcesRootPath().relativize(this)

    private inline fun <T> accessFileSystem(block: (FileSystem) -> T): T {
        val fs = beforeAccess()
        return try {
            block(fs)
        } finally {
            afterAccess()
        }
    }

    private fun beforeAccess(): FileSystem = fsLock.withLock {
        val counter = nestedAccessCounter.get()

        if (counter == 0) {
            useCount++
            lastUse = System.currentTimeMillis()
            if (fs == null)
                fs = openFileSystem()
        }

        nestedAccessCounter.set(counter + 1)
        fs!! // This should never be null
    }

    private fun afterAccess(): Unit = fsLock.withLock {
        val counter = nestedAccessCounter.get()
        nestedAccessCounter.set(counter - 1)

        if (counter == 1) {
            useCount--
            lastUse = System.currentTimeMillis()
            nestedAccessCounter.remove()
        }
    }

    private fun openFileSystem(): FileSystem {
        if (!isJar) {
            val fs = FileSystems.getDefault()
            this.fs = fs
            return fs
        }

        val uri = URI.create(resourcesRoot.toString().substringBefore('!'))
        val fs = FileSystems.newFileSystem(uri, emptyMap<String, Any>())
        this.fs = fs
        return fs
    }

    private fun setupFileSystemClosureTask() {
        // We can't close the default filesystem.
        if (!isJar)
            return

        fun closeFileSystem(): Unit = fsLock.withLock {
            val fs = fs
            if (fs == null || useCount > 0)
                return@withLock

            if (lastUse + 30000 < System.currentTimeMillis()) {
                fs.close()
                this.fs = null
            }
        }

        Thread.ofVirtual().start {
            while (true) {
                closeFileSystem()
                Thread.sleep(30000)
            }
        }
    }

    private fun isApplicationRunningInJar(): Boolean {
        return when (val protocol = resourcesRoot.protocol) {
            "jar" -> true
            "file" -> false
            else -> throw IllegalStateException("Unsupported resources protocol $protocol")
        }
    }
}