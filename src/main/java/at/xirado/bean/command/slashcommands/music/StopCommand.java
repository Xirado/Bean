package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StopCommand extends SlashCommand
{
    public StopCommand()
    {
        setCommandData(new CommandData("stop", "Disconnects the bot and clears the queue."));
        addCommandFlags(CommandFlag.DJ_ONLY, CommandFlag.MUST_BE_IN_SAME_VC, CommandFlag.MUST_BE_IN_VC);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        AudioManager audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected())
        {
            event.replyEmbeds(EmbedUtil.warningEmbed("I am not connected to a voice channel!")).queue();
            return;
        }
        String name = audioManager.getConnectedChannel().getName();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.replyEmbeds(EmbedUtil.defaultEmbed("Disconnected from **"+name+"**!")).queue();
    }
}
