package at.Xirado.Bean.Listeners;

import at.Xirado.Bean.Misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class LogListeners extends ListenerAdapter {

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        Guild g = event.getGuild();
        TextChannel logchannel = Util.getLogChannel(g);
        User unbanned = event.getUser();
        Util.removeBan(g.getIdLong(),unbanned.getIdLong());
        if(logchannel != null)
        {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(Color.decode("#FEFEFE"))
                    .setTitle("User unbanned")
                    .addField("User", unbanned.getAsMention(), false)
                    .setFooter("ID: "+unbanned.getIdLong()+" | "+unbanned.getAsTag())
                    .setThumbnail(unbanned.getEffectiveAvatarUrl());
            logchannel.sendMessage(builder.build()).queue();
        }
    }
}
