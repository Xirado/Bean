package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.FormatUtil;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.misc.Util;
import at.xirado.bean.misc.objects.TrackInfo;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.events.interaction.ApplicationCommandAutocompleteEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.data.DataArray;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class PlayCommand extends SlashCommand
{
    public PlayCommand()
    {
        setCommandData(new CommandData("play", "plays a video")
                .addOptions(new OptionData(OptionType.STRING, "query", "youtube search term or an url that is supported", true).setAutoComplete(true))
        );
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC);

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
        query = (query.startsWith("http://") || query.startsWith("https://")) ? query : "ytsearch:" + query;
        Bean.getInstance().getAudioManager().getPlayerManager().loadItemOrdered(guildAudioPlayer, query, new AudioLoadResultHandler()
        {
            @Override
            public void trackLoaded(AudioTrack track)
            {
                track.setUserData(new TrackInfo(event.getUser().getIdLong()));
                event.getHook().sendMessageEmbeds(MusicUtil.getAddedToQueueMessage(guildAudioPlayer, track)).queue();
                guildAudioPlayer.getScheduler().queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
                if (playlist.getTracks().size() == 1 || playlist.isSearchResult())
                {
                    AudioTrack single = (playlist.getSelectedTrack() == null) ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
                    single.setUserData(new TrackInfo(event.getUser().getIdLong()));
                    event.getHook().sendMessageEmbeds(MusicUtil.getAddedToQueueMessage(guildAudioPlayer, single)).queue();
                    guildAudioPlayer.getScheduler().queue(single);
                    return;
                }
                String amount = "Added **" + playlist.getTracks().size() + "** tracks to the queue! (**" + FormatUtil.formatTime(playlist.getTracks().stream().map(AudioTrack::getDuration).reduce(0L, Long::sum)) + "**)";
                if (guildAudioPlayer.getPlayer().getPlayingTrack() == null)
                {
                    amount += "\n**Now playing** " + Util.titleMarkdown(playlist.getTracks().get(0));
                }
                event.getHook().sendMessageEmbeds(ctx.getSimpleEmbed(amount)).queue();
                playlist.getTracks().forEach(track ->
                {
                    track.setUserData(new TrackInfo(event.getUser().getIdLong()));
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
                event.getHook().sendMessageEmbeds(ctx.getSimpleEmbed("An error occurred while loading track!\n`" + exception.getMessage() + "`")).queue();
            }
        });
    }

    @Override
    public void handleAutocomplete(@NotNull ApplicationCommandAutocompleteEvent event) throws Exception
    {
        OptionMapping query = event.getOption("query");
        if (query != null && query.isFocused())
        {
            if (query.getAsString().length() == 0)
            {
                event.deferChoices(Collections.emptyList()).queue();
                return;
            }
            String url = "https://clients1.google.com/complete/search?client=youtube&gs_ri=youtube&hl=en&ds=yt&q="+query.getAsString().replace("\\s+", "%20");
            Request request = new Request.Builder().url(url).build();
            Call call = Bean.getInstance().getOkHttpClient().newCall(request);
            Response response = call.execute();
            if (!response.isSuccessful())
            {
                event.deferChoices(Collections.singletonList(new Command.Choice(query.getAsString(), query.getAsString()))).queue();
                return;
            }
            Set<Command.Choice> choices = new LinkedHashSet<>();
            String string = response.body().string();
            string = string.substring(19, string.length()-1);
            DataArray array = DataArray.fromJson(string).getArray(1);
            array.stream(DataArray::getArray)
                    .limit(10)
                    .forEach(x -> choices.add(new Command.Choice(x.getString(0), x.getString(0))));
            if (choices.size() == 0)
                choices.add(new Command.Choice(query.getAsString(), query.getAsString()));
            event.deferChoices(choices).queue();
        }
    }
}
