package at.xirado.bean

import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

fun main() {
    //DecoroutinatorJvmApi.install()

    startKoin {
        modules(Module().module)
    }
}