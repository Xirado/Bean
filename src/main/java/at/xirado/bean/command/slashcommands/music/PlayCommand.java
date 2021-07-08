package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.FormatUtil;
import at.xirado.bean.misc.Util;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

public class PlayCommand extends SlashCommand
{
    public PlayCommand()
    {
        setCommandData(new CommandData("play", "plays a song")
                .addOption(OptionType.STRING, "query", "either a term to search for or an url that is supported", true)
        );
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.FAIL_IF_IN_DIFFERENT_VC);

    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        event.deferReply().queue();
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        AudioManager manager = event.getGuild().getAudioManager();
        if (manager.getConnectedChannel() == null)
        {
            manager.openAudioConnection(voiceState.getChannel());
            if (voiceState.getChannel() instanceof StageChannel)
            {
                event.getGuild().requestToSpeak();
            }
        }
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        if (manager.getSendingHandler() == null)
            manager.setSendingHandler(guildAudioPlayer.getSendHandler());
        String query = event.getOption("query").getAsString();
        query = query.startsWith("http://") || query.startsWith("https://") ? query : "ytsearch:" + query;
        Bean.getInstance().getAudioManager().getPlayerManager().loadItemOrdered(guildAudioPlayer, query, new AudioLoadResultHandler()
        {
            @Override
            public void trackLoaded(AudioTrack track)
            {
                track.setUserData(event.getUser().getIdLong());
                event.getHook().sendMessageEmbeds(MusicUtil.getAddedToQueueMessage(guildAudioPlayer, track)).queue();
                guildAudioPlayer.getScheduler().queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
                if (playlist.getTracks().size() == 1 || playlist.isSearchResult())
                {
                    AudioTrack single = (playlist.getSelectedTrack() == null) ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
                    single.setUserData(event.getUser().getIdLong());
                    event.getHook().sendMessageEmbeds(MusicUtil.getAddedToQueueMessage(guildAudioPlayer, single)).queue();
                    guildAudioPlayer.getScheduler().queue(single);
                    return;
                }
                String amount = "Added `" + playlist.getTracks().size() + "` tracks! (`" + FormatUtil.formatTime(playlist.getTracks().stream().map(AudioTrack::getDuration).reduce(0L, Long::sum)) + "`)";
                if (guildAudioPlayer.getPlayer().getPlayingTrack() == null)
                {
                    amount += "\n**Now playing**: " + Util.titleMarkdown(playlist.getTracks().get(0));
                }
                event.getHook().sendMessageEmbeds(ctx.getSimpleEmbed(amount)).queue();
                playlist.getTracks().forEach(track ->
                {
                    track.setUserData(event.getUser().getIdLong());
                    guildAudioPlayer.getScheduler().queue(track);
                });
            }

            @Override
            public void noMatches()
            {
                event.getHook().sendMessageEmbeds(ctx.getSimpleEmbed("Sorry, i couldn't find anything!")).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception)
            {
                event.getHook().sendMessageEmbeds(ctx.getSimpleEmbed("An error occurred while loading a track!\n`" + exception.getMessage() + "`")).queue();
            }
        });
    }
}
