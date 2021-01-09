// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.utils.AttachmentOption;

public class DebugCmd extends OwnerCommand
{
    private static final String[] PROPERTIES;
    private final Bot bot;
    
    public DebugCmd(final Bot bot) {
        this.bot = bot;
        this.name = "debug";
        this.help = "shows debug info";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(final CommandEvent event) {
        final StringBuilder sb = new StringBuilder();
        sb.append("```\nSystem Properties:");
        for (final String key : DebugCmd.PROPERTIES) {
            sb.append("\n  ").append(key).append(" = ").append(System.getProperty(key));
        }
        sb.append("\n\nJMusicBot Information:").append("\n  Version = ").append(OtherUtil.getCurrentVersion()).append("\n  Owner = ").append(this.bot.getConfig().getOwnerId()).append("\n  Prefix = ").append(this.bot.getConfig().getPrefix()).append("\n  AltPrefix = ").append(this.bot.getConfig().getAltPrefix()).append("\n  MaxSeconds = ").append(this.bot.getConfig().getMaxSeconds()).append("\n  NPImages = ").append(this.bot.getConfig().useNPImages()).append("\n  SongInStatus = ").append(this.bot.getConfig().getSongInStatus()).append("\n  StayInChannel = ").append(this.bot.getConfig().getStay()).append("\n  UseEval = ").append(this.bot.getConfig().useEval()).append("\n  UpdateAlerts = ").append(this.bot.getConfig().useUpdateAlerts());
        sb.append("\n\nDependency Information:").append("\n  JDA Version = ").append(JDAInfo.VERSION).append("\n  JDA-Utilities Version = ").append(JDAUtilitiesInfo.VERSION).append("\n  Lavaplayer Version = ").append(PlayerLibrary.VERSION);
        final long total = Runtime.getRuntime().totalMemory() / 1024L / 1024L;
        final long used = total - Runtime.getRuntime().freeMemory() / 1024L / 1024L;
        sb.append("\n\nRuntime Information:").append("\n  Total Memory = ").append(total).append("\n  Used Memory = ").append(used);
        sb.append("\n\nDiscord Information:").append("\n  ID = ").append(event.getJDA().getSelfUser().getId()).append("\n  Guilds = ").append(event.getJDA().getGuildCache().size()).append("\n  Users = ").append(event.getJDA().getUserCache().size());
        sb.append("\n```");
        if (event.isFromType(ChannelType.PRIVATE) || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ATTACH_FILES)) {
            event.getChannel().sendFile(sb.toString().getBytes(), "debug_information.txt", new AttachmentOption[0]).queue();
        }
        else {
            event.reply("Debug Information: " + sb.toString());
        }
    }
    
    static {
        PROPERTIES = new String[] { "java.version", "java.vm.name", "java.vm.specification.version", "java.runtime.name", "java.runtime.version", "java.specification.version", "os.arch", "os.name" };
    }
}
