package at.xirado.bean

import at.xirado.bean.database.DatabaseConfig
import at.xirado.bean.jda.JDAConfig
import at.xirado.bean.util.tomlDecode
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

@Module
@ComponentScan("at.xirado.bean")
class Module {
    @Single fun provideConfig(): Config = readConfig()
    @Single fun provideDatabaseConfig(config: Config): DatabaseConfig = config.database
    @Single fun provideJDAConfig(config: Config): JDAConfig = config.jda
}

private fun readConfig(): Config {
    val path = Path("config.toml")

    if (!path.exists())
        throw IllegalStateException("config.toml does not exist")

    val content = path.readText()
    return tomlDecode(content)
}