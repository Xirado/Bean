package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.BasicAutocompletionChoice;
import at.xirado.bean.data.Hints;
import at.xirado.bean.data.IAutocompleteChoice;
import at.xirado.bean.data.SearchEntry;
import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.lavaplayer.SpotifyTrack;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.FormatUtil;
import at.xirado.bean.misc.MusicUtil;
import at.xirado.bean.misc.Util;
import at.xirado.bean.misc.objects.TrackInfo;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class PlayCommand extends SlashCommand
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayCommand.class);

    public PlayCommand()
    {
        setCommandData(Commands.slash("play", "Plays a track from YouTube, Soundcloud, Spotify, and more.")
                .addOptions(new OptionData(OptionType.STRING, "query", "Youtube search term or a URL that is supported.", true).setAutoComplete(true))
                .addOptions(new OptionData(OptionType.STRING, "provider", "Provider to search in. (Ignore if you put a direct link)", false)
                        .addChoice("Youtube (Default)", "ytsearch:")
                        .addChoice("Spotify", "spsearch:")
                        .addChoice("Soundcloud", "scsearch:")
                        .addChoice("Youtube Music", "ytmsearch:")
                )
        );
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC, CommandFlag.REQUIRES_LAVALINK_NODE);
    }

    private static final MessageEmbed BOOKMARK_HINT_EMBED =
            EmbedUtil.defaultEmbedBuilder("Bookmark songs and playlists using the **/bookmark** command!\nHaving to always type the link to your favourite youtube or spotify playlist is annoying, isn't it?")
                    .setAuthor(Bean.getInstance().getShardManager().getShards().get(0).getSelfUser().getAsTag(), null, Bean.getInstance().getShardManager().getShards().get(0).getSelfUser().getAvatarUrl())
                    .setImage("https://bean.bz/assets/hints/bookmark.png")
                    .setTitle("Tip")
                    .build();

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        JdaLink link = Bean.getInstance().getLavalink().getLink(event.getGuild());
        event.deferReply().queue();
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        AudioManager manager = event.getGuild().getAudioManager();
        if (manager.getConnectedChannel() == null)
        {
            try
            {
                link.connect(voiceState.getChannel());
            }
            catch (PermissionException exception)
            {
                event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("I do not have permission to join this channel!")).queue();
                return;
            }
            if (voiceState.getChannel() instanceof StageChannel)
            {
                event.getGuild().requestToSpeak();
            }
        }
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        String query = event.getOption("query").getAsString();
        boolean isDirectUrl = query.startsWith("http://") || query.startsWith("https://");
        if (!isDirectUrl)
        {
            String provider;
            OptionMapping providerOption = event.getOption("provider");
            if (providerOption == null)
                provider = "ytsearch:";
            else
                provider = providerOption.getAsString();
            query = provider + query;
        }
        long userId = event.getUser().getIdLong();
        long channelId = event.getChannel().getIdLong();
        String rawQuery = event.getOption("query").getAsString();
        link.getRestClient().loadItem(query, new AudioLoadResultHandler()
        {
            @Override
            public void trackLoaded(AudioTrack track)
            {
                TrackInfo trackInfo = new TrackInfo(userId, channelId)
                        .setTrackUrl(track.getInfo().uri);
                track.setUserData(trackInfo);
                event.getHook().sendMessageEmbeds(MusicUtil.getAddedToQueueMessage(guildAudioPlayer, track)).queue();
                boolean isBookmarked = BookmarkCommand.getBookmark(event.getUser().getIdLong(), track.getInfo().uri) != null;
                if (!Hints.hasAcknowledged(userId, "bookmark") && !isBookmarked)
                {
                    event.getHook().sendMessageEmbeds(BOOKMARK_HINT_EMBED)
                            .setEphemeral(true)
                            .addActionRow(Util.getDontShowThisAgainButton("bookmark"))
                            .queue();
                    Hints.sentUserHint(userId, "bookmark");
                }
                guildAudioPlayer.getScheduler().queue(track);
                SearchEntry entry = new SearchEntry(track.getInfo().title, rawQuery, false);
                if (!isDuplicate(member.getIdLong(), entry.getName()) && !isBookmarked)
                    addSearchEntry(member.getIdLong(), entry);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
                if (playlist.isSearchResult())
                {
                    AudioTrack single = (playlist.getSelectedTrack() == null) ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
                    TrackInfo trackInfo = new TrackInfo(userId, channelId)
                            .setTrackUrl(single.getInfo().uri);
                    single.setUserData(trackInfo);
                    event.getHook().sendMessageEmbeds(MusicUtil.getAddedToQueueMessage(guildAudioPlayer, single)).queue();
                    guildAudioPlayer.getScheduler().queue(single);
                    SearchEntry entry = new SearchEntry(event.getOption("query").getAsString(), event.getOption("query").getAsString(), false);
                    if (!isDuplicate(member.getIdLong(), entry.getName()))
                        addSearchEntry(member.getIdLong(), entry);
                    return;
                }
                boolean isBookmarked = BookmarkCommand.getBookmark(event.getUser().getIdLong(), rawQuery) != null;
                String amount = "Added **" + playlist.getTracks().size() + "** tracks to the queue! (**" + FormatUtil.formatTime(playlist.getTracks().stream().map(AudioTrack::getDuration).reduce(0L, Long::sum)) + "**)";
                if (guildAudioPlayer.getPlayer().getPlayingTrack() == null)
                {
                    amount += "\n**Now playing** " + Util.titleMarkdown(playlist.getTracks().get(0));
                }
                EmbedBuilder embed = EmbedUtil.defaultEmbedBuilder(amount);
                if (playlist.getTracks().get(0) instanceof YoutubeAudioTrack)
                    embed.setThumbnail("https://img.youtube.com/vi/" + playlist.getTracks().get(0).getIdentifier() + "/mqdefault.jpg");
                else if (playlist.getTracks().get(0) instanceof SpotifyTrack track)
                    embed.setThumbnail(track.getArtworkURL());
                embed.setFooter(playlist.getName());
                event.getHook().sendMessageEmbeds(embed.build()).queue();
                if (!Hints.hasAcknowledged(userId, "bookmark") && !isBookmarked)
                {
                    event.getHook().sendMessageEmbeds(BOOKMARK_HINT_EMBED)
                            .setEphemeral(true)
                            .addActionRow(Util.getDontShowThisAgainButton("bookmark"))
                            .queue();
                    Hints.sentUserHint(userId, "bookmark");
                }
                playlist.getTracks().forEach(track ->
                {
                    TrackInfo trackInfo = new TrackInfo(userId, channelId)
                            .setTrackUrl(track.getInfo().uri)
                            .setPlaylistName(playlist.getName())
                            .setPlaylistUrl(rawQuery);
                    track.setUserData(trackInfo);
                    guildAudioPlayer.getScheduler().queue(track);
                });
                SearchEntry entry = new SearchEntry(playlist.getName(), event.getOption("query").getAsString(), true);
                if (!isDuplicate(member.getIdLong(), entry.getName()) && !isBookmarked)
                    addSearchEntry(member.getIdLong(), entry);
            }

            @Override
            public void noMatches()
            {
                event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("Sorry, i couldn't find anything matching your search!")).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception)
            {
                event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("An error occurred while loading track!\n`" + exception.getMessage() + "`")).queue();
            }
        });
    }

    @Override
    public void handleAutocomplete(@NotNull CommandAutoCompleteInteractionEvent event) throws Exception
    {
        long userId = event.getUser().getIdLong();
        if (event.getFocusedOption().getName().equals("query"))
        {
            AutoCompleteQuery query = event.getFocusedOption();
            List<IAutocompleteChoice> result = new ArrayList<>();
            boolean hasSearchEntries = hasSearchEntries(userId);
            if (query.getValue().isEmpty())
            {
                result.addAll(BookmarkCommand.getBookmarks(userId, false));
                if (!hasSearchEntries)
                {
                    event.replyChoices(
                            result.stream().map(IAutocompleteChoice::toCommandAutocompleteChoice).collect(Collectors.toList())
                    ).queue(s ->
                    {
                    }, e ->
                    {
                    });
                    return;
                }
                List<SearchEntry> searchEntries = getSearchHistory(event.getMember().getIdLong(), false);
                List<String> valueList = result.stream().map(IAutocompleteChoice::getValue).collect(Collectors.toList());
                searchEntries.stream()
                        .filter(x -> !valueList.contains(x.getValue()))
                        .limit(25 - result.size())
                        .limit(7)
                        .forEachOrdered(result::add);
                event.replyChoices(
                        result.stream().map(IAutocompleteChoice::toCommandAutocompleteChoice).collect(Collectors.toList())
                ).queue(s ->
                {
                }, e ->
                {
                });
                return;
            }
            BookmarkCommand.getBookmarks(userId, true)
                    .stream()
                    .filter(choice -> StringUtils.startsWithIgnoreCase(choice.getName(), query.getValue()))
                    .limit(25)
                    .forEach(result::add);
            List<String> valueList = result.stream().map(IAutocompleteChoice::getValue).collect(Collectors.toList());
            List<String> alreadyAdded = new ArrayList<>();
            if (hasSearchEntries)
            {
                List<SearchEntry> searchEntries = getSearchHistory(event.getMember().getIdLong(), true);
                searchEntries
                        .stream()
                        .filter(x -> !valueList.contains(x.getValue()))
                        .filter(choice -> StringUtils.startsWithIgnoreCase(choice.getName(), query.getValue()))
                        .limit(Util.zeroIfNegative(25 - result.size()))
                        .forEachOrdered(entry ->
                        {
                            result.add(entry);
                            alreadyAdded.add(entry.getName().toLowerCase(Locale.ROOT));
                        });
            }
            List<String> ytMusicResults = getYoutubeMusicSearchResults(query.getValue());
            ytMusicResults.stream()
                    .filter(x -> !alreadyAdded.contains(x.toLowerCase(Locale.ROOT)))
                    .limit(Util.zeroIfNegative(25 - result.size() - alreadyAdded.size()))
                    .forEach(x -> result.add(new BasicAutocompletionChoice(x, x)));
            if (result.size() == 0)
                result.add(new BasicAutocompletionChoice(query.getValue(), query.getValue()));
            event.replyChoices(
                    result.stream().map(IAutocompleteChoice::toCommandAutocompleteChoice).collect(Collectors.toList())
            ).queue(s ->
            {
            }, e ->
            {
            });
        }
    }

    private List<SearchEntry> getSearchHistory(long userId, boolean all)
    {
        try (ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM searchqueries WHERE user_id = ? ORDER BY searched_at desc" + (all ? "" : " LIMIT 25")).addParameter(userId).executeQuery())
        {
            List<SearchEntry> entries = new ArrayList<>();
            while (rs.next())
                entries.add(new SearchEntry(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist")));
            return entries;
        }
        catch (SQLException throwables)
        {
            LOGGER.warn("Could not get search history from " + userId + "!", throwables);
            return Collections.emptyList();
        }
    }

    private List<SearchEntry> getMatchingEntries(long userId, String prefix, boolean all)
    {
        try (ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM searchqueries WHERE user_id = ? AND name like ? ORDER BY searched_at desc" + (all ? "" : " LIMIT 25")).addParameters(userId, prefix + "%").executeQuery())
        {
            List<SearchEntry> entries = new ArrayList<>();
            while (rs.next())
                entries.add(new SearchEntry(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist")));
            return entries;
        }
        catch (SQLException throwables)
        {
            LOGGER.warn("Could not get search history from " + userId + "!", throwables);
            return Collections.emptyList();
        }
    }

    private boolean isDuplicate(long userId, String name)
    {
        try (ResultSet rs = new SQLBuilder("SELECT 1 FROM searchqueries WHERE user_id = ? AND name = ?").addParameters(userId, name).executeQuery())
        {
            return rs.next();
        }
        catch (SQLException ex)
        {
            LOGGER.error("Could not check if duplicate exists! (User: " + userId + ", Term: " + name + ")", ex);
            return true;
        }
    }

    private boolean hasSearchEntries(long userId)
    {
        try (ResultSet rs = new SQLBuilder("SELECT 1 FROM searchqueries WHERE user_id = ?").addParameters(userId).executeQuery())
        {
            return rs.next();
        }
        catch (SQLException ex)
        {
            LOGGER.error("Could not check if user " + userId + " has search entries!", ex);
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
        }
        catch (SQLException ex)
        {
            LOGGER.error("Could not add search entry for user " + userId + "!", ex);

        }
    }

    public List<String> getYoutubeMusicSearchResults(String query)
    {
        try
        {
            DataObject config = Bean.getInstance().getConfig();
            if (config.isNull("innertube_api_key"))
                return Collections.emptyList();
            if (config.isNull("innertube_request_body"))
            {
                LOGGER.warn("Missing innertube_request_body config property!");
                return Collections.emptyList();
            }
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("music.youtube.com")
                    .setPath("/youtubei/v1/music/get_search_suggestions")
                    .addParameter("key", config.getString("innertube_api_key"))
                    .build();
            DataObject body = config.getObject("innertube_request_body");
            body.put("input", query);
            RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), body.toString());
            Request request = new Request.Builder()
                    .url(uri.toURL())
                    .post(requestBody)
                    .addHeader("referer", "https://music.youtube.com/")
                    .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
                    .addHeader("content-type", "application/json")
                    .build();
            Call call = Bean.getInstance().getOkHttpClient().newCall(request);
            Response response = call.execute();
            DataObject responseBody = DataObject.fromJson(response.body().string());
            response.close();
            Optional<DataArray> optContents = responseBody.optArray("contents");
            if (!optContents.isPresent())
                return Collections.emptyList();
            DataObject renderer = optContents.get().getObject(0).getObject("searchSuggestionsSectionRenderer");
            DataArray contents = renderer.optArray("contents").orElse(DataArray.empty());
            List<String> results = new ArrayList<>();
            contents.stream(DataArray::getObject).forEach(content ->
            {
                String result = content.getObject("searchSuggestionRenderer").getObject("navigationEndpoint").getObject("searchEndpoint").getString("query");
                results.add(result);
            });
            return results;
        }
        catch (Exception e)
        {
            LOGGER.warn("Innertube API is broken!", e);
        }
        return Collections.emptyList();
    }
}
