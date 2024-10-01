package at.xirado.bean

import at.xirado.bean.jda.JDAService
import dev.reformator.stacktracedecoroutinator.jvm.DecoroutinatorJvmApi
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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