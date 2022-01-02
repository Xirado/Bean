package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.Util;
import at.xirado.bean.music.GuildAudioPlayer;
import lavalink.client.io.Link;
import lavalink.client.io.jda.JdaLavalink;
import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JoinCommand extends SlashCommand
{
    public JoinCommand()
    {
        setCommandData(new CommandData("join", "Makes the bot join your current channel."));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.REQUIRES_LAVALINK_NODE);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState.getChannel() == null)
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("You must be listening in a voice channel to run this command!")).queue();
            return;
        }
        GuildVoiceState state = event.getGuild().getSelfMember().getVoiceState();
        if (state.getChannel() != null)
        {
            VoiceChannel channel = state.getChannel();
            if (voiceState.getChannel().getIdLong() == channel.getIdLong())
            {
                event.replyEmbeds(EmbedUtil.errorEmbed("I already joined this channel!")).queue();
                return;
            }
            if (Util.getListeningUsers(channel) > 0)
            {
                event.replyEmbeds(EmbedUtil.errorEmbed("I am already playing music in **"+channel.getName()+"**!")).queue();
                return;
            }
        }
        GuildAudioPlayer audioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        try
        {
            audioPlayer.getLink().connect(voiceState.getChannel());
        } catch (PermissionException exception)
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("I do not have permission to join this channel!")).queue();
            return;
        }
        event.replyEmbeds(EmbedUtil.successEmbed("Joined <#"+voiceState.getChannel().getIdLong()+">!")).queue();
    }
}
