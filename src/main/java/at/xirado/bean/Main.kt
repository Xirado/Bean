package at.xirado.bean

import dev.reformator.stacktracedecoroutinator.jvm.DecoroutinatorJvmApi
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

lateinit var koin: Koin
    private set

fun main() {
    DecoroutinatorJvmApi.install()
    val app = startKoin {
        modules(Module().module)
    }

    koin = app.koin
}