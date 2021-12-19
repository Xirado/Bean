package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.SearchEntry;
import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.misc.EmbedUtil;
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
import net.dv8tion.jda.api.exceptions.PermissionException;
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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayCommand extends SlashCommand
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayCommand.class);

    public PlayCommand()
    {
        setCommandData(new CommandData("play", "Plays a track from YouTube or SoundCloud.")
                .addOptions(new OptionData(OptionType.STRING, "query", "Youtube search term or a URL that is supported.", true).setAutoComplete(true))
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
            try
            {
                manager.openAudioConnection(voiceState.getChannel());
            } catch (PermissionException exception)
            {
                event.replyEmbeds(EmbedUtil.errorEmbed("I do not have permission to join this channel!")).queue();
                return;
            }
            if (voiceState.getChannel() instanceof StageChannel)
            {
                event.getGuild().requestToSpeak();
            }
        }
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        if (manager.getSendingHandler() == null)
            manager.setSendingHandler(guildAudioPlayer.getSendHandler());
        String query = event.getOption("query").getAsString();
        boolean isDirectUrl = query.startsWith("http://") || query.startsWith("https://");
        query = isDirectUrl ? query : "ytsearch:" + query;
        Bean.getInstance().getAudioManager().getPlayerManager().loadItemOrdered(guildAudioPlayer, query, new AudioLoadResultHandler()
        {
            @Override
            public void trackLoaded(AudioTrack track)
            {
                track.setUserData(new TrackInfo(event.getUser().getIdLong()));
                event.getHook().sendMessageEmbeds(MusicUtil.getAddedToQueueMessage(guildAudioPlayer, track)).queue();
                Bean.getInstance().getExecutor().schedule(() -> guildAudioPlayer.getScheduler().queue(track), 1, TimeUnit.SECONDS);
                SearchEntry entry = new SearchEntry(track.getInfo().title, event.getOption("query").getAsString(), false);
                if (!isDuplicate(member.getIdLong(), entry.getName()))
                    addSearchEntry(member.getIdLong(), entry);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
                if (playlist.getTracks().size() == 1 || playlist.isSearchResult())
                {
                    AudioTrack single = (playlist.getSelectedTrack() == null) ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
                    single.setUserData(new TrackInfo(event.getUser().getIdLong()));
                    event.getHook().sendMessageEmbeds(MusicUtil.getAddedToQueueMessage(guildAudioPlayer, single)).queue();
                    Bean.getInstance().getExecutor().schedule(() -> guildAudioPlayer.getScheduler().queue(single), 1, TimeUnit.SECONDS);
                    SearchEntry entry = new SearchEntry(event.getOption("query").getAsString(), event.getOption("query").getAsString(), false);
                    if (!isDuplicate(member.getIdLong(), entry.getName()))
                        addSearchEntry(member.getIdLong(), entry);
                    return;
                }
                String amount = "Added **" + playlist.getTracks().size() + "** tracks to the queue! (**" + FormatUtil.formatTime(playlist.getTracks().stream().map(AudioTrack::getDuration).reduce(0L, Long::sum)) + "**)";
                if (guildAudioPlayer.getPlayer().getPlayingTrack() == null)
                {
                    amount += "\n**Now playing** " + Util.titleMarkdown(playlist.getTracks().get(0));
                }
                event.getHook().sendMessageEmbeds(ctx.getSimpleEmbed(amount)).queue();
                Bean.getInstance().getExecutor().schedule(() -> {
                    playlist.getTracks().forEach(track ->
                    {
                        track.setUserData(new TrackInfo(event.getUser().getIdLong()));
                        guildAudioPlayer.getScheduler().queue(track);
                    });
                }, 1, TimeUnit.SECONDS);
                SearchEntry entry = new SearchEntry(playlist.getName(), event.getOption("query").getAsString(), true);
                if (!isDuplicate(member.getIdLong(), entry.getName()))
                    addSearchEntry(member.getIdLong(), entry);
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
                if (!hasSearchEntries(event.getMember().getIdLong()))
                {
                    event.deferChoices(Collections.emptyList()).queue();
                    return;
                }
                List<SearchEntry> entries = getSearchHistory(event.getMember().getIdLong(), false);
                List<Command.Choice> choices = new ArrayList<>();
                entries.forEach(x -> choices.add(x.toCommandAutocompleteChoice()));
                event.deferChoices(choices).queue();
                return;
            }
            String url = "https://clients1.google.com/complete/search?client=youtube&gs_ri=youtube&hl=en&ds=yt&q="+ URLEncoder.encode(query.getAsString(), StandardCharsets.UTF_8);
            Request request = new Request.Builder().url(url).build();
            Call call = Bean.getInstance().getOkHttpClient().newCall(request);
            Response response = call.execute();
            if (!response.isSuccessful())
            {
                if (!hasSearchEntries(event.getMember().getIdLong()))
                {
                    event.deferChoices(Collections.singletonList(new Command.Choice(query.getAsString(), query.getAsString()))).queue();
                    return;
                }
                List<SearchEntry> searchEntries = getSearchHistory(event.getMember().getIdLong(), true);
                List<Command.Choice> choices = new ArrayList<>();
                searchEntries
                        .stream()
                        .filter(choice -> StringUtils.startsWithIgnoreCase(choice.getName(), query.getAsString()))
                        .limit(25)
                        .forEachOrdered(entry -> choices.add(entry.toCommandAutocompleteChoice()));
                event.deferChoices(choices).queue();
                return;
            }
            Set<Command.Choice> choices = new LinkedHashSet<>();
            List<String> alreadyAdded = new ArrayList<>();
            if (hasSearchEntries(event.getMember().getIdLong()))
            {
                List<SearchEntry> searchEntries = getSearchHistory(event.getMember().getIdLong(), true);
                searchEntries
                        .stream()
                        .filter(choice -> StringUtils.startsWithIgnoreCase(choice.getName(), query.getAsString()))
                        .limit(15)
                        .forEachOrdered(entry -> {
                            choices.add(entry.toCommandAutocompleteChoice());
                            alreadyAdded.add(entry.getName().toLowerCase(Locale.ROOT));
                        });
            }
            String string = response.body().string();
            string = string.substring(19, string.length()-1);
            DataArray array = DataArray.fromJson(string).getArray(1);
            array.stream(DataArray::getArray)
                    .filter(x -> !alreadyAdded.contains(x.getString(0).toLowerCase(Locale.ROOT)))
                    .limit(10)
                    .forEach(x -> choices.add(new Command.Choice(x.getString(0), x.getString(0))));
            if (choices.size() == 0)
                choices.add(new Command.Choice(query.getAsString(), query.getAsString()));
            event.deferChoices(choices).queue();
        }
    }

    private List<SearchEntry> getSearchHistory(long userId, boolean all)
    {
        try(ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM searchqueries WHERE user_id = ? ORDER BY searched_at desc"+(all ? "" : " LIMIT 25")).addParameter(userId).executeQuery())
        {
            List<SearchEntry> entries = new ArrayList<>();
            while (rs.next())
                entries.add(new SearchEntry(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist")));
            return entries;
        } catch (SQLException throwables)
        {
            LOGGER.warn("Could not get search history from "+userId+"!", throwables);
            return Collections.emptyList();
        }
    }

    private List<SearchEntry> getMatchingEntries(long userId, String prefix, boolean all)
    {
        try(ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM searchqueries WHERE user_id = ? AND name like ? ORDER BY searched_at desc"+(all ? "" : " LIMIT 25")).addParameters(userId, prefix+"%").executeQuery())
        {
            List<SearchEntry> entries = new ArrayList<>();
            while (rs.next())
                entries.add(new SearchEntry(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist")));
            return entries;
        } catch (SQLException throwables)
        {
            LOGGER.warn("Could not get search history from "+userId+"!", throwables);
            return Collections.emptyList();
        }
    }

    private boolean isDuplicate(long userId, String name)
    {
        try(ResultSet rs = new SQLBuilder("SELECT 1 FROM searchqueries WHERE user_id = ? AND name = ?").addParameters(userId, name).executeQuery())
        {
            return rs.next();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not check if duplicate exists! (User: "+userId+", Term: "+name+")", ex);
            return true;
        }
    }

    private boolean hasSearchEntries(long userId)
    {
        try(ResultSet rs = new SQLBuilder("SELECT 1 FROM searchqueries WHERE user_id = ?").addParameters(userId).executeQuery())
        {
            return rs.next();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not check if user "+userId+" has search entries!", ex);
            return false;
        }
    }

    private void addSearchEntry(long userId, SearchEntry entry)
    {
        try
        {
            new SQLBuilder("INSERT INTO searchqueries (user_id, searched_at, name, value, playlist) values (?,?,?,?,?)")
                    .addParameters(userId, System.currentTimeMillis(), entry.getName(), entry.getValue(), entry.isPlaylist())
                    .execute();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not add search entry for user "+userId+"!", ex);

        }
    }
}
