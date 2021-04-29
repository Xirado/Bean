package at.xirado.bean.music;

import at.xirado.bean.Bean;
import at.xirado.bean.commandutil.CommandArgument;
import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PlayCommand extends Command
{
    public PlayCommand()
    {
        super("play", "Plays/Resumes a song", "play [Title/URL]");
        setAliases("p");
        setCommandCategory(CommandCategory.MUSIC);
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
            context.reply(CommandContext.LOADING_EMOTE + " Loading... `[" + asWhole + "]`", m -> Bean.instance.musicinstance.getPlayerManager().loadItemOrdered(event.getGuild(), asWhole, new ResultHandler(m, context, false)));
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