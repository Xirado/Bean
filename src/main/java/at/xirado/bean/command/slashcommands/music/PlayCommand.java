package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.AutoComplete;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.BasicAutocompletionChoice;
import at.xirado.bean.data.IAutocompleteChoice;
import at.xirado.bean.data.SearchEntry;
import at.xirado.bean.data.content.*;
import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.lavaplayer.SpotifyTrack;
import at.xirado.bean.misc.*;
import at.xirado.bean.misc.objects.TrackInfo;
import at.xirado.bean.music.GuildAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PlayCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayCommand.class);

    public PlayCommand() {
        setCommandData(Commands.slash("play", "Plays a track from YouTube, Soundcloud, Spotify, and more.")
                .addOptions(new OptionData(OptionType.STRING, "query", "Youtube search term or a URL that is supported.", true).setAutoComplete(true))
                .addOptions(new OptionData(OptionType.STRING, "provider", "Provider to search in. (Ignore if you put a direct link)", false)
                        .addChoice("Youtube (Default)", "ytsearch:")
                        .addChoice("Spotify", "spsearch:")
                        .addChoice("Soundcloud", "scsearch:")
                        .addChoice("Youtube Music", "ytmsearch:")
                )
                .addOptions(new OptionData(OptionType.BOOLEAN, "shuffle", "Whether to enable shuffle."))
        );
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC, CommandFlag.REQUIRES_LAVALINK_NODE);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        event.deferReply(true).queue();
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        AudioManager manager = event.getGuild().getAudioManager();
        if (manager.getConnectedChannel() == null) {
            try {
                manager.openAudioConnection(voiceState.getChannel());
            } catch (PermissionException exception) {
                event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("I do not have permission to join this channel!")).queue();
                return;
            }
            if (voiceState.getChannel() instanceof StageChannel) {
                event.getGuild().requestToSpeak();
            }
        }
        AudioPlayerManager playerManager = Bean.getInstance().getAudioManager().getPlayerManager();
        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        if (event.getOption("shuffle") != null)
            guildAudioPlayer.getScheduler().setShuffle(event.getOption("shuffle").getAsBoolean());

        String query = event.getOption("query").getAsString();
        boolean isDirectUrl = query.startsWith("http://") || query.startsWith("https://");
        if (!isDirectUrl) {
            String provider;
            OptionMapping providerOption = event.getOption("provider");
            if (providerOption == null)
                provider = "ytsearch:";
            else
                provider = providerOption.getAsString();
            query = provider + query;
        }
        long userId = event.getUser().getIdLong();
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        String rawQuery = event.getOption("query").getAsString();
        DismissableContentManager contentManager = Bean.getInstance().getDismissableContentManager();
        boolean isYoutube = query.startsWith("ytsearch:");
        boolean hasBookmarks = !BookmarkCommand.getBookmarks(event.getUser().getIdLong(), false).isEmpty();
        playerManager.loadItem(query, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                TrackInfo trackInfo = new TrackInfo(userId, guildId, channelId)
                        .setTrackUrl(track.getInfo().uri);
                track.setUserData(trackInfo);
                event.getHook().sendMessageEmbeds(MusicUtil.getAddedToQueueMessage(guildAudioPlayer, track)).queue();
                boolean isBookmarked = BookmarkCommand.getBookmark(event.getUser().getIdLong(), track.getInfo().uri) != null;
                handleDismissableContent(contentManager, userId, hasBookmarks, event.getHook());
                guildAudioPlayer.getScheduler().queue(track);
                if (guildAudioPlayer.getOpenPlayer() == null)
                    guildAudioPlayer.playerSetup((GuildMessageChannel) event.getChannel(), (s) -> {
                    }, e -> {
                    });
                else
                    guildAudioPlayer.forcePlayerUpdate();
                SearchEntry entry = new SearchEntry(track.getInfo().title, rawQuery, false);
                if (!isDuplicate(member.getIdLong(), entry.getName()) && !isBookmarked)
                    addSearchEntry(member.getIdLong(), entry);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    AudioTrack single = (playlist.getSelectedTrack() == null) ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
                    TrackInfo trackInfo = new TrackInfo(userId, guildId, channelId)
                            .setTrackUrl(single.getInfo().uri);
                    single.setUserData(trackInfo);
                    event.getHook().sendMessageEmbeds(MusicUtil.getAddedToQueueMessage(guildAudioPlayer, single)).queue();
                    guildAudioPlayer.getScheduler().queue(single);
                    if (guildAudioPlayer.getOpenPlayer() == null)
                        guildAudioPlayer.playerSetup((GuildMessageChannel) event.getChannel(), (s) -> {
                        }, e -> {
                        });
                    else
                        guildAudioPlayer.forcePlayerUpdate();
                    SearchEntry entry = new SearchEntry(event.getOption("query").getAsString(), event.getOption("query").getAsString(), false);
                    if (!isDuplicate(member.getIdLong(), entry.getName()))
                        addSearchEntry(member.getIdLong(), entry);
                    return;
                }
                boolean isBookmarked = BookmarkCommand.getBookmark(event.getUser().getIdLong(), rawQuery) != null;
                String amount = "Added **" + playlist.getTracks().size() + "** tracks to the queue! (**" + FormatUtil.formatTime(playlist.getTracks().stream().map(AudioTrack::getDuration).reduce(0L, Long::sum)) + "**)";
                List<AudioTrack> tracks = new ArrayList<>(playlist.getTracks());
                if (guildAudioPlayer.getScheduler().isShuffle())
                    Collections.shuffle(tracks);
                if (guildAudioPlayer.getPlayer().getPlayingTrack() == null) {
                    amount += "\n**Now playing** " + Util.titleMarkdown(tracks.get(0));
                }
                EmbedBuilder embed = EmbedUtil.defaultEmbedBuilder(amount);
                if (tracks.get(0) instanceof YoutubeAudioTrack)
                    embed.setThumbnail("https://img.youtube.com/vi/" + tracks.get(0).getIdentifier() + "/mqdefault.jpg");
                else if (tracks.get(0) instanceof SpotifyTrack track)
                    embed.setThumbnail(track.getArtworkURL());
                embed.setFooter(playlist.getName());
                event.getHook().sendMessageEmbeds(embed.build()).queue();
                handleDismissableContent(contentManager, userId, hasBookmarks, event.getHook());
                tracks.forEach(track ->
                {
                    TrackInfo trackInfo = new TrackInfo(userId, guildId, channelId)
                            .setTrackUrl(track.getInfo().uri)
                            .setPlaylistName(playlist.getName())
                            .setPlaylistUrl(rawQuery);
                    track.setUserData(trackInfo);
                    guildAudioPlayer.getScheduler().queue(track);
                });
                if (guildAudioPlayer.getOpenPlayer() == null)
                    guildAudioPlayer.playerSetup((GuildMessageChannel) event.getChannel(), tracks.get(0), (s) -> {
                    }, e -> {
                    });
                else
                    guildAudioPlayer.forcePlayerUpdate();
                SearchEntry entry = new SearchEntry(playlist.getName(), event.getOption("query").getAsString(), true);
                if (!isDuplicate(member.getIdLong(), entry.getName()) && !isBookmarked)
                    addSearchEntry(member.getIdLong(), entry);
            }

            @Override
            public void noMatches() {
                event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("Sorry, i couldn't find anything matching your search!")).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("An error occurred while loading track!\n`" + exception.getMessage() + "`")).queue();
            }
        });
    }

    @AutoComplete(optionName = "query")
    public void onQueryAutocomplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        if (event.getFocusedOption().getName().equals("query")) {
            AutoCompleteQuery query = event.getFocusedOption();
            List<IAutocompleteChoice> result = new ArrayList<>();
            boolean hasSearchEntries = hasSearchEntries(userId);
            if (query.getValue().isEmpty()) {
                result.addAll(BookmarkCommand.getBookmarks(userId, false));
                if (!hasSearchEntries) {
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
                List<String> valueList = result.stream().map(IAutocompleteChoice::getValue).toList();
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
            if (hasSearchEntries) {
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
            if (result.size() == 0 && query.getValue().length() <= 100)
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

    private List<SearchEntry> getSearchHistory(long userId, boolean all) {
        try (ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM search_queries WHERE user_id = ? ORDER BY searched_at desc" + (all ? "" : " LIMIT 25")).addParameter(userId).executeQuery()) {
            List<SearchEntry> entries = new ArrayList<>();
            while (rs.next()) {
                SearchEntry entry = new SearchEntry(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist"));
                if (entry.getValue().length() <= 100)
                    entries.add(entry);
            }
            return entries;
        } catch (SQLException throwables) {
            LOGGER.warn("Could not get search history from {}!", userId, throwables);
            return Collections.emptyList();
        }
    }

    private List<SearchEntry> getMatchingEntries(long userId, String prefix, boolean all) {
        try (ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM search_queries WHERE user_id = ? AND name like ? ORDER BY searched_at desc" + (all ? "" : " LIMIT 25")).addParameters(userId, prefix + "%").executeQuery()) {
            List<SearchEntry> entries = new ArrayList<>();
            while (rs.next())
                entries.add(new SearchEntry(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist")));
            return entries;
        } catch (SQLException throwables) {
            LOGGER.warn("Could not get search history from {}!", userId, throwables);
            return Collections.emptyList();
        }
    }

    private boolean isDuplicate(long userId, String name) {
        try (ResultSet rs = new SQLBuilder("SELECT 1 FROM search_queries WHERE user_id = ? AND name = ?").addParameters(userId, name).executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            LOGGER.error("Could not check if duplicate exists! (User: {}, Term: {})", userId, name, ex);
            return true;
        }
    }

    private boolean hasSearchEntries(long userId) {
        try (ResultSet rs = new SQLBuilder("SELECT 1 FROM search_queries WHERE user_id = ?").addParameters(userId).executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            LOGGER.error("Could not check if user {} has search entries!", userId, ex);
            return false;
        }
    }

    private void addSearchEntry(long userId, SearchEntry entry) {
        try {
            new SQLBuilder("INSERT INTO search_queries (user_id, searched_at, name, value, playlist) values (?,?,?,?,?)")
                    .addParameters(userId, System.currentTimeMillis(), entry.getName(), entry.getValue(), entry.isPlaylist())
                    .execute();
        } catch (SQLException ex) {
            LOGGER.error("Could not add search entry for user {}!", userId, ex);

        }
    }

    private void handleDismissableContent(DismissableContentManager contentManager, long userId, boolean hasBookmarks, InteractionHook hook) {
        if (!contentManager.hasProgress(userId, BookmarkDismissableContent.class)) {
            if (hasBookmarks) {
                contentManager.createDismissableContent(userId, BookmarkDismissableContent.class, DismissableState.AWARE);
            } else {
                DismissableProgress progress = contentManager.createDismissableContent(
                        userId, BookmarkDismissableContent.class, DismissableState.SEEN
                );

                MessageEmbedDismissable dismissable = (MessageEmbedDismissable) progress.getDismissable();
                hook.sendMessageEmbeds(dismissable.get())
                        .setEphemeral(true)
                        .queue();
            }
        }
    }

    public List<String> getYoutubeMusicSearchResults(String query) {
        try {
            List<String> results = YoutubeUtils.INSTANCE.getYoutubeMusicSearchResults(query);
            return results;
        } catch (Exception e) {
            LOGGER.warn("Innertube API is broken!", e);
        }
        return Collections.emptyList();
    }
}
