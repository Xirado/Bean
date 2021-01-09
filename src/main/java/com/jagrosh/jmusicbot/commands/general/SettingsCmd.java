// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class SettingsCmd extends Command
{
    private static final String EMOJI = "\ud83c\udfa7";
    
    public SettingsCmd(final Bot bot) {
        this.name = "settings";
        this.help = "shows the bots settings";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }
    
    @Override
    protected void execute(final CommandEvent event) {
        final Settings s = event.getClient().getSettingsFor(event.getGuild());
        final MessageBuilder builder = new MessageBuilder().append((CharSequence)"\ud83c\udfa7 **").append((CharSequence)FormatUtil.filter(event.getSelfUser().getName())).append((CharSequence)"** settings:");
        final TextChannel tchan = s.getTextChannel(event.getGuild());
        final VoiceChannel vchan = s.getVoiceChannel(event.getGuild());
        final Role role = s.getRole(event.getGuild());
        final EmbedBuilder ebuilder = new EmbedBuilder().setColor(event.getSelfMember().getColor()).setDescription("Text Channel: " + ((tchan == null) ? "Any" : ("**#" + tchan.getName() + "**")) + "\nVoice Channel: " + ((vchan == null) ? "Any" : ("**" + vchan.getName() + "**")) + "\nDJ Role: " + ((role == null) ? "None" : ("**" + role.getName() + "**")) + "\nCustom Prefix: " + ((s.getPrefix() == null) ? "None" : ("`" + s.getPrefix() + "`")) + "\nRepeat Mode: **" + (s.getRepeatMode() ? "On" : "Off") + "**\nDefault Playlist: " + ((s.getDefaultPlaylist() == null) ? "None" : ("**" + s.getDefaultPlaylist() + "**"))).setFooter(event.getJDA().getGuilds().size() + " servers | " + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inVoiceChannel()).count() + " audio connections", null);
        event.getChannel().sendMessage(builder.setEmbed(ebuilder.build()).build()).queue();
    }
}
