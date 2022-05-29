package at.xirado.bean.io.config

import at.xirado.bean.util.getLog
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import java.util.stream.Collectors

private val log = getLog<BeanConfiguration>()

class BeanConfiguration(dataObject: DataObject) {
    val discordToken: String
    val devMode: Boolean
    val devGuilds: List<Long>
    val devUsers: List<Long>
    val dbConfig: DataObject

    init {
        discordToken = dataObject.getStringOrThrow("discord_token")
        devMode = dataObject.getBoolean("dev_mode", false)
        devGuilds = dataObject.optArray("dev_guilds")
            .orElseGet(DataArray::empty)
            .stream(DataArray::getLong)
            .collect(Collectors.toUnmodifiableList())

        devUsers = dataObject.optArray("dev_users")
            .orElseGet(DataArray::empty)
            .stream(DataArray::getLong)
            .collect(Collectors.toUnmodifiableList())

        if (devMode && devGuilds.isEmpty())
            log.warn("Dev mode is enabled but no guilds are specified. You will not see any commands!")

        dbConfig = dataObject.optObject("database").orElseGet(DataObject::empty)
    }
}