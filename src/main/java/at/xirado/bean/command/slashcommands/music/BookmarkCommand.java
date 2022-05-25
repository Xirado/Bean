package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.Bookmark;
import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.objects.TrackInfo;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.LavalinkSocket;
import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BookmarkCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkCommand.class);

    public BookmarkCommand() {
        setCommandData(Commands.slash("bookmark", "Bookmark a song or playlist from Youtube or Soundcloud")
                .addSubcommands(new SubcommandData("add", "Adds a bookmark")
                        .addOptions(new OptionData(OptionType.STRING, "url", "URL to bookmark (must be a valid url that can be played)", true))
                )
                .addSubcommands(new SubcommandData("remove", "Removes a bookmark")
                        .addOptions(new OptionData(OptionType.STRING, "bookmark", "Bookmark to remove").setRequired(true).setAutoComplete(true))
                )
                .addSubcommands(new SubcommandData("add_current", "Bookmarks the currently playing track (or playlist)"))
        );
    }


    private static final MessageEmbed SINGLE_OR_PLAYLIST_EMBED =
            EmbedUtil.defaultEmbedBuilder("Do you want to bookmark the whole playlist, or just this single track?")
                    .setFooter("(You have 30 seconds to decide)")
                    .build();

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        long userId = event.getUser().getIdLong();
        event.deferReply(true).queue();
        switch (event.getSubcommandName().toLowerCase(Locale.ROOT)) {
            case "add" -> {
                String url = event.getOption("url").getAsString();
                LavalinkSocket socket = ctx.getAvailableNode();
                if (isDuplicate(userId, url)) {
                    event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("You already have a bookmark for that URL!")).queue();
                    return;
                }
                socket.getRestClient().loadItem(url, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        Bookmark entry = new Bookmark(track.getInfo().title, url, false);
                        addBookmark(userId, entry);
                        event.getHook().sendMessageEmbeds(EmbedUtil.defaultEmbed("Added bookmark: **" + track.getInfo().title + "**")).queue();
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        Bookmark entry = new Bookmark(playlist.getName(), url, true);
                        addBookmark(userId, entry);
                        event.getHook().sendMessageEmbeds(EmbedUtil.defaultEmbed("Added bookmark: **" + playlist.getName() + "**")).queue();
                    }

                    @Override
                    public void noMatches() {
                        event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("Sorry, that is not a valid track i can play!")).queue();
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                        event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("Sorry, that is not a valid track i can play!\n" + exception.getLocalizedMessage())).queue();
                    }
                });
            }

            case "remove" -> {
                String bookmarkUrl = event.getOption("bookmark").getAsString();
                Bookmark bookmark = getBookmark(userId, bookmarkUrl);
                if (bookmark == null) {
                    event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("Could not find a bookmark with a matching URL!")).queue();
                    return;
                }
                deleteBookmark(userId, bookmark);
                event.getHook().sendMessageEmbeds(EmbedUtil.defaultEmbed("Removed bookmark **" + bookmark.getName() + "**!")).queue();
            }

            case "add_current" -> {
                JdaLink link = Bean.getInstance().getLavalink().getLink(event.getGuild());
                AudioTrack currentTrack = link.getPlayer().getPlayingTrack();
                if (currentTrack == null) {
                    event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("There is nothing playing!")).queue();
                    return;
                }
                TrackInfo currentTrackInfo = currentTrack.getUserData(TrackInfo.class);
                if (!currentTrackInfo.isFromPlaylist()) {
                    if (getBookmark(userId, currentTrackInfo.getTrackUrl()) != null) {
                        event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("You already have this track bookmarked!")).queue();
                        return;
                    }
                    Bookmark entry = new Bookmark(currentTrack.getInfo().title, currentTrackInfo.getTrackUrl(), false);
                    addBookmark(userId, entry);
                    event.getHook().sendMessageEmbeds(EmbedUtil.defaultEmbed("Added bookmark: **" + currentTrack.getInfo().title + "**")).queue();
                    return;
                }
                String trackUrl = currentTrackInfo.getTrackUrl();
                String playlistUrl = currentTrackInfo.getPlaylistUrl();
                String name = currentTrack.getInfo().title;
                String playlistName = currentTrackInfo.getPlaylistName();
                String interactionId = event.getInteraction().getId();
                Button singleTrack = Button.primary(interactionId + ":single", "Single Track");
                Button wholePlaylist = Button.primary(interactionId + ":playlist", "Playlist");
                event.getHook().sendMessageEmbeds(SINGLE_OR_PLAYLIST_EMBED)
                        .addActionRow(singleTrack, wholePlaylist)
                        .setEphemeral(true)
                        .queue((hook) -> Bean.getInstance().getEventWaiter().waitForEvent(
                                ButtonInteractionEvent.class,
                                (e) ->
                                {
                                    if (!e.getComponentId().startsWith(interactionId))
                                        return false;
                                    return e.getComponentId().contains(":");
                                },
                                (e) ->
                                {
                                    int colonIndex = e.getComponentId().indexOf(":");
                                    String mode = e.getComponentId().substring(colonIndex + 1);
                                    switch (mode) {
                                        case "single" -> {
                                            if (getBookmark(userId, trackUrl) != null) {
                                                e.editMessageEmbeds(EmbedUtil.errorEmbed("You already have this track bookmarked!"))
                                                        .setActionRows(Collections.emptyList())
                                                        .queue();
                                                return;
                                            }
                                            Bookmark entry = new Bookmark(name, trackUrl, false);
                                            addBookmark(userId, entry);
                                            e.editMessageEmbeds(EmbedUtil.defaultEmbed("Added bookmark: **" + name + "**"))
                                                    .setActionRows(Collections.emptyList())
                                                    .queue();
                                        }
                                        case "playlist" -> {
                                            if (getBookmark(userId, playlistUrl) != null) {
                                                e.editMessageEmbeds(EmbedUtil.errorEmbed("You already have this playlist bookmarked!"))
                                                        .setActionRows(Collections.emptyList())
                                                        .queue();
                                                return;
                                            }
                                            Bookmark entry = new Bookmark(playlistName, playlistUrl, true);
                                            addBookmark(userId, entry);
                                            e.editMessageEmbeds(EmbedUtil.defaultEmbed("Added bookmark: **" + playlistName + "**"))
                                                    .setActionRows(Collections.emptyList())
                                                    .queue();
                                        }
                                    }
                                },
                                30, TimeUnit.SECONDS, () ->
                                {
                                }
                        ));
            }
        }
    }

    @Override
    public void handleAutocomplete(@NotNull CommandAutoCompleteInteractionEvent event) throws Exception {
        long userId = event.getUser().getIdLong();
        if (event.getFocusedOption().getName().equals("bookmark")) {
            var mapping = event.getFocusedOption();
            if (!hasBookmarks(userId)) {
                event.replyChoices(Collections.emptyList()).queue();
                return;
            }
            String input = mapping.getValue();
            if (input.isEmpty()) {
                List<Bookmark> bookmarks = getBookmarks(userId, false);
                event.replyChoices(
                        bookmarks.stream().map(Bookmark::toChoice).collect(Collectors.toList())
                ).queue();
                return;
            }
            List<Bookmark> bookmarks = getMatchingBookmarks(userId, input, false);
            event.replyChoices(
                    bookmarks.stream().map(Bookmark::toChoice).collect(Collectors.toList())
            ).queue();
        }
    }

    public static Bookmark getBookmark(long userId, String url) {
        try (ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM bookmarks WHERE user_id = ? AND value = ?", userId, url).executeQuery()) {
            if (rs.next())
                return new Bookmark(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist"));
            return null;
        } catch (SQLException throwables) {
            LOGGER.error("Could not get bookmark {} from user {}!", url, userId);
            return null;
        }
    }

    public static List<Bookmark> getBookmarks(long userId, boolean all) {
        try (ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM bookmarks WHERE user_id = ? ORDER BY added_at desc" + (all ? "" : " LIMIT 25")).addParameter(userId).executeQuery()) {
            List<Bookmark> entries = new ArrayList<>();
            while (rs.next()) {
                Bookmark bookmark = new Bookmark(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist"));
                if (bookmark.getValue().length() <= 100)
                    entries.add(bookmark);
            }
            Collections.reverse(entries);
            return entries;
        } catch (SQLException throwables) {
            LOGGER.warn("Could not get bookmarks from {}!", userId, throwables);
            return Collections.emptyList();
        }
    }

    public static List<Bookmark> getMatchingBookmarks(long userId, String prefix, boolean all) {
        try (ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM bookmarks WHERE user_id = ? AND name like ? ORDER BY added_at desc" + (all ? "" : " LIMIT 25")).addParameters(userId, prefix + "%").executeQuery()) {
            List<Bookmark> entries = new ArrayList<>();
            while (rs.next())
                entries.add(new Bookmark(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist")));
            return entries;
        } catch (SQLException throwables) {
            LOGGER.warn("Could not get bookmarks from {}!", userId, throwables);
            return Collections.emptyList();
        }
    }

    public static void deleteBookmark(long userId, Bookmark bookmark) {
        try {
            new SQLBuilder("DELETE FROM bookmarks WHERE user_id = ? AND value = ?", userId, bookmark.getValue())
                    .execute();
        } catch (SQLException ex) {
            LOGGER.error("Could not remove bookmark!");
        }
    }

    private boolean isDuplicate(long userId, String name) {
        try (ResultSet rs = new SQLBuilder("SELECT 1 FROM bookmarks WHERE user_id = ? AND value = ?", userId, name).executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            LOGGER.error("Could not check if duplicate exists! (User: {}, Term: {})", userId, name, ex);
            return true;
        }
    }

    private boolean hasBookmarks(long userId) {
        try (ResultSet rs = new SQLBuilder("SELECT 1 FROM bookmarks WHERE user_id = ?").addParameters(userId).executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            LOGGER.error("Could not check if user {} has bookmarks!", userId, ex);
            return false;
        }
    }

    private void addBookmark(long userId, Bookmark entry) {
        try {
            new SQLBuilder("INSERT INTO bookmarks (user_id, added_at, name, value, playlist) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE name = ?")
                    .addParameters(userId, System.currentTimeMillis(), entry.getName(), entry.getValue(), entry.isPlaylist(), entry.getName())
                    .execute();
        } catch (SQLException ex) {
            LOGGER.error("Could not add bookmark for user {}!", userId, ex);
        }
    }
}
