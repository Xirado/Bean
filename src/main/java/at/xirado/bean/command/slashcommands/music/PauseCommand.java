package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PauseCommand extends SlashCommand
{
    public PauseCommand()
    {
        setCommandData(new CommandData("pause", "pause the currently playing song"));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.DJ_ONLY, CommandFlag.FAIL_IF_IN_DIFFERENT_VC);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioPlayer player = guildAudioPlayer.getPlayer();
        if (player.getPlayingTrack() == null)
        {
            ctx.replyError("I'm currently not playing any music!").queue();
        }
        if (player.isPaused())
        {
            ctx.replyError("The player is already paused! Use `/resume`").queue();
            return;
        }
        player.setPaused(true);
        ctx.sendSimpleEmbed("The player is now paused!");
    }
}
