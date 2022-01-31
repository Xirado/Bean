package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PauseCommand extends SlashCommand
{
    public PauseCommand()
    {
        setCommandData(new CommandData("pause", "Pauses the currently playing track."));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.DJ_ONLY, CommandFlag.MUST_BE_IN_SAME_VC);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        JdaLink link = Bean.getInstance().getLavalink().getLink(event.getGuild());
        LavalinkPlayer player = link.getPlayer();
        if (player.getPlayingTrack() == null)
        {
            ctx.replyError("I'm currently not playing any music!").queue();
            return;
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
