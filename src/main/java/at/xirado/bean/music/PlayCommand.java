package at.xirado.bean.music;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandArgument;
import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

public class PlayCommand extends Command
{
    public PlayCommand(JDA jda)
    {
        super(jda);
        this.invoke = "play";
        this.description = "Plays/Resumes a song";
        this.usage = "play [Title/URL]";
        this.aliases = Arrays.asList("p");
        this.commandType = CommandType.MUSIC;
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        CommandArgument commandArgument = context.getArguments();
        String asWhole = commandArgument.toString(0);
        String[] args = commandArgument.toStringArray();
        if(ResultHandler.init(context))
            return;
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (args.length != 0 || !event.getMessage().getAttachments().isEmpty()) {
            handler.getPlayer().setVolume(70);
            if(!event.getGuild().getAudioManager().isSelfDeafened())
            {
                event.getGuild().getAudioManager().setSelfDeafened(true);
            }
            final String argument = (asWhole.startsWith("<") && asWhole.endsWith(">")) ? asWhole.substring(1, asWhole.length() - 1) : (asWhole.isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : asWhole);
            context.reply(CommandContext.LOADING_EMOTE + " Loading... `[" + asWhole + "]`", m -> DiscordBot.instance.musicinstance.getPlayerManager().loadItemOrdered(event.getGuild(), asWhole, new ResultHandler(m, context, false)));
            return;
        }
        if(handler.getPlayer().getPlayingTrack() != null)
        {
            if(handler.getPlayer().isPaused())
            {
                if (context.isDJ()) {
                    handler.getPlayer().setPaused(false);
                    context.replySuccess("Resumed **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**.");
                }
                else {
                    context.replyError("Only DJs can unpause the player!");
                }
            }else
            {
                context.replyWarning("The player is not paused!");
            }
            return;
        }
        context.replyErrorUsage();
    }
}