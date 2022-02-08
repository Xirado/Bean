package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.music.GuildAudioPlayer;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class ResumeCommand extends SlashCommand
{
    public ResumeCommand()
    {
        setCommandData(Commands.slash("resume", "Resumes the player if paused."));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.DJ_ONLY);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        JdaLink link = Bean.getInstance().getLavalink().getLink(event.getGuild());
        LavalinkPlayer player = link.getPlayer();
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());

        if (player.getPlayingTrack() == null)
        {
            ctx.replyError("I'm currently not playing any music!").setEphemeral(true).queue();
        }
        if (!player.isPaused())
        {
            ctx.replyError("The player is not paused!").setEphemeral(true).queue();
            return;
        }
        player.setPaused(false);
        guildAudioPlayer.forcePlayerComponentsUpdate();
        ctx.sendSimpleEphemeralEmbed("Resumed!");
    }
}
