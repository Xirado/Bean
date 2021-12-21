package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.Bookmark;
import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.misc.EmbedUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.LavalinkSocket;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ApplicationCommandAutocompleteEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BookmarkCommand extends SlashCommand
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkCommand.class);

    public BookmarkCommand()
    {
        setCommandData(new CommandData("bookmark", "Bookmark a song or playlist from Youtube or Soundcloud")
                .addSubcommands(new SubcommandData("create", "Creates a bookmark")
                        .addOptions(new OptionData(OptionType.STRING, "url", "URL to bookmark (must be a valid url that can be played)", true))
                )
                .addSubcommands(new SubcommandData("remove", "Removes a bookmark")
                        .addOptions(new OptionData(OptionType.STRING, "bookmark", "Bookmark to remove").setRequired(true).setAutoComplete(true))
                )
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        long userId = event.getUser().getIdLong();
        switch (event.getSubcommandName().toLowerCase(Locale.ROOT))
        {
            case "create" -> {
                String url = event.getOption("url").getAsString();
                if (!url.startsWith("https://"))
                {
                    event.replyEmbeds(EmbedUtil.errorEmbed("This is not a valid URL!")).queue();
                    return;
                }
                LavalinkSocket socket = ctx.getAvailableNode();
                if (isDuplicate(userId, url))
                {
                    event.replyEmbeds(EmbedUtil.errorEmbed("You already have a bookmark for that URL!")).queue();
                    return;
                }
                socket.getRestClient().loadItem(url, new AudioLoadResultHandler()
                {
                    @Override
                    public void trackLoaded(AudioTrack track)
                    {

                        Bookmark entry = new Bookmark(track.getInfo().title, url, false);
                        addBookmark(userId, entry);
                        event.replyEmbeds(EmbedUtil.defaultEmbed("Added bookmark: **"+track.getInfo().title+"**")).queue();
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist)
                    {
                        Bookmark entry = new Bookmark(playlist.getName(), url, true);
                        addBookmark(userId, entry);
                        event.replyEmbeds(EmbedUtil.defaultEmbed("Added bookmark: **"+playlist.getName()+"**")).queue();
                    }

                    @Override
                    public void noMatches()
                    {
                        event.replyEmbeds(EmbedUtil.errorEmbed("Sorry, that is not a valid track i can play!")).queue();
                    }

                    @Override
                    public void loadFailed(FriendlyException exception)
                    {
                        event.replyEmbeds(EmbedUtil.errorEmbed("Sorry, that is not a valid track i can play!\n"+exception.getLocalizedMessage())).queue();
                    }
                });
            }

            case "remove" -> {
                String bookmarkUrl = event.getOption("bookmark").getAsString();
                Bookmark bookmark = getBookmark(userId, bookmarkUrl);
                if (bookmark == null)
                {
                    event.replyEmbeds(EmbedUtil.errorEmbed("Could not find a bookmark with a matching URL!")).queue();
                    return;
                }
                deleteBookmark(userId, bookmark);
                event.replyEmbeds(EmbedUtil.defaultEmbed("Removed bookmark **"+bookmark.getName()+"**!")).queue();
            }
        }
    }

    @Override
    public void handleAutocomplete(@NotNull ApplicationCommandAutocompleteEvent event) throws Exception
    {
        long userId = event.getUser().getIdLong();
        OptionMapping mapping = event.getOption("bookmark");
        if (mapping.isFocused())
        {
            if (!hasBookmarks(userId))
            {
                event.deferChoices(Collections.emptyList()).queue();
                return;
            }
            String input = mapping.getAsString();
            if (input.isEmpty())
            {
                List<Bookmark> bookmarks = getBookmarks(userId, false);
                event.deferChoices(
                  bookmarks.stream().map(Bookmark::toCommandAutocompleteChoice).collect(Collectors.toList())
                ).queue();
                return;
            }
            List<Bookmark> bookmarks = getMatchingBookmarks(userId, input, false);
            event.deferChoices(
                    bookmarks.stream().map(Bookmark::toCommandAutocompleteChoice).collect(Collectors.toList())
            ).queue();
        }
    }

    public static Bookmark getBookmark(long userId, String url)
    {
        try(ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM bookmarks WHERE user_id = ? AND value = ?", userId, url).executeQuery())
        {
            if (rs.next())
                return new Bookmark(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist"));
            return null;
        } catch (SQLException throwables)
        {
            LOGGER.error("Could not get bookmark {} from user {}!", url, userId);
            return null;
        }
    }

    public static List<Bookmark> getBookmarks(long userId, boolean all)
    {
        try(ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM bookmarks WHERE user_id = ? ORDER BY added_at desc"+(all ? "" : " LIMIT 25")).addParameter(userId).executeQuery())
        {
            List<Bookmark> entries = new ArrayList<>();
            while (rs.next())
                entries.add(new Bookmark(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist")));
            Collections.reverse(entries);
            return entries;
        } catch (SQLException throwables)
        {
            LOGGER.warn("Could not get bookmarks from "+userId+"!", throwables);
            return Collections.emptyList();
        }
    }

    public static List<Bookmark> getMatchingBookmarks(long userId, String prefix, boolean all)
    {
        try(ResultSet rs = new SQLBuilder("SELECT name, value, playlist FROM bookmarks WHERE user_id = ? AND name like ? ORDER BY added_at desc"+(all ? "" : " LIMIT 25")).addParameters(userId, prefix+"%").executeQuery())
        {
            List<Bookmark> entries = new ArrayList<>();
            while (rs.next())
                entries.add(new Bookmark(rs.getString("name"), rs.getString("value"), rs.getBoolean("playlist")));
            return entries;
        } catch (SQLException throwables)
        {
            LOGGER.warn("Could not get bookmarks from "+userId+"!", throwables);
            return Collections.emptyList();
        }
    }

    public static void deleteBookmark(long userId, Bookmark bookmark)
    {
        try
        {
            new SQLBuilder("DELETE FROM bookmarks WHERE user_id = ? AND value = ?", userId, bookmark.getValue())
                    .execute();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not remove bookmark!");
        }
    }

    private boolean isDuplicate(long userId, String name)
    {
        try(ResultSet rs = new SQLBuilder("SELECT 1 FROM bookmarks WHERE user_id = ? AND value = ?", userId, name).executeQuery())
        {
            return rs.next();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not check if duplicate exists! (User: "+userId+", Term: "+name+")", ex);
            return true;
        }
    }

    private boolean hasBookmarks(long userId)
    {
        try(ResultSet rs = new SQLBuilder("SELECT 1 FROM bookmarks WHERE user_id = ?").addParameters(userId).executeQuery())
        {
            return rs.next();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not check if user "+userId+" has bookmarks!", ex);
            return false;
        }
    }

    private void addBookmark(long userId, Bookmark entry)
    {
        try
        {
            new SQLBuilder("INSERT INTO bookmarks (user_id, added_at, name, value, playlist) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE name = ?")
                    .addParameters(userId, System.currentTimeMillis(), entry.getName(), entry.getValue(), entry.isPlaylist(), entry.getName())
                    .execute();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not add bookmark for user "+userId+"!", ex);

        }
    }
}
