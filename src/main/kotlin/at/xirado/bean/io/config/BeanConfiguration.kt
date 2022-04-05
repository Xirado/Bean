package at.xirado.bean.io.config

import net.dv8tion.jda.api.utils.data.DataObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BeanConfiguration(dataObject: DataObject) {

    companion object {
        private val log = LoggerFactory.getLogger(BeanConfiguration::class.java) as Logger
    }

    val discordToken: String

    init {
        discordToken = dataObject.getStringOrThrow("discord_token")
    }

}