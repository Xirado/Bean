package at.xirado.bean.interaction.command.slash.misc

import at.xirado.bean.Application
import at.xirado.bean.interaction.SlashCommand
import at.xirado.bean.util.replyError
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType.USER

class UserInfoCommand(private val application: Application) : SlashCommand("userinfo", "get useful information of a user") {

    init {
        option(type = USER, name = "member", description = "the member to get information about", required = true)
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val time = System.currentTimeMillis()
        val member = event.getOption<Member>("member")

        if (member == null) {
            event.replyError("This user is not on this server!").queue()
            return
        }

        val embedBuilder = EmbedBuilder()
            .setTitle("User-Info for ${member.user.asTag}")
            .setColor(Application.BEAN_COLOR_ACCENT)
            .setThumbnail(member.effectiveAvatarUrl)

        val userId = member.idLong

        val profile = member.user.retrieveProfile().await()

        var profileField = "User-ID: ${userId}\n"

        profileField += "[Avatar](${member.user.effectiveAvatarUrl})\n"

        if (member.avatarUrl != null) {
            profileField += "[Guild-Avatar](${member.avatarUrl})\n"
        }

        if (profile.bannerUrl != null) {
            profileField += "[Banner](${profile.bannerUrl})\n"
        }

        if (profile.accentColor != null) {
            profileField += "Accent-Color: 0x${Integer.toHexString(profile.accentColorRaw)}\n"
        }

        embedBuilder.addField("Profile", profileField, true)

        val permissions = member.permissions
        val permissionsJoined = permissions.joinToString(", ") { it.getName() }

        embedBuilder.addField("Permissions", permissionsJoined, false)

        val joinedDate = member.timeJoined.toEpochSecond()
        val createdDate = member.user.timeCreated.toEpochSecond()

        embedBuilder.addField("Dates", "Server Joined: <t:$joinedDate>\nAccount Created: <t:$createdDate>", true)

        embedBuilder.addField("Boosting", if (member.isBoosting) "Since <t:${member.timeBoosted!!.toEpochSecond()}>" else "Not boosting", true)

        event.replyEmbeds(embedBuilder.build()).queue()


    }
}