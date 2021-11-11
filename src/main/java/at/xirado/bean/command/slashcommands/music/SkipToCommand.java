package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.BlockingQueue;

public class SkipToCommand extends SlashCommand
{
    public SkipToCommand()
    {
        setCommandData(new CommandData("skipto", "Skips to a specified track in the queue.")
                .addOption(OptionType.INTEGER, "index", "Index to skip to.", true)
        );
        addCommandFlags(CommandFlag.DJ_ONLY, CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        long index = event.getOption("index").getAsLong();
        if (index > Integer.MAX_VALUE)
        {
            ctx.sendSimpleEmbed("This is not a valid integer!");
            return;
        }
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        if (guildAudioPlayer.getPlayer().getPlayingTrack() == null)
        {
            ctx.sendSimpleEmbed("There is no music playing!");
            return;
        }
        BlockingQueue<AudioTrack> queue = guildAudioPlayer.getScheduler().getQueue();
        if (queue.size() == 0)
        {
            ctx.sendSimpleEmbed("There is nothing in the queue!");
            return;
        }
        if (index < 1 || index > queue.size())
        {
            ctx.sendSimpleEmbed("Index must be a valid integer between 1 and "+queue.size()+"!");
            return;
        }
        for (int i = 0; i < index-1; i++)
        {
            queue.remove();
        }
        guildAudioPlayer.getScheduler().nextTrack();
        ctx.sendSimpleEmbed("**Skipped!** Now playing: `" + guildAudioPlayer.getPlayer().getPlayingTrack().getInfo().title + "`");
    }

}
