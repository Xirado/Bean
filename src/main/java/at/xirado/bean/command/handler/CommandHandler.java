package at.xirado.bean.command.handler;

import at.xirado.bean.Bean;
import at.xirado.bean.command.Command;
import at.xirado.bean.command.CommandArgument;
import at.xirado.bean.command.CommandContext;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.commands.*;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.translation.LanguageLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class CommandHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    private final ConcurrentMap<String, Command> registeredCommands = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

    public CommandHandler()
    {
        registerCommand(new Settings());
    }

    private void registerCommand(Command command)
    {
        String name = command.getName();
        if (registeredCommands.containsKey(name))
        {
            LOGGER.error("Command \"" + name + "\" could not be registered because a command (or alias) with this name already exists!");
            return;
        }
        registeredCommands.put(name, command);
        if (command.getAliases() != null && command.getAliases().size() >= 1)
        {
            for (String alias : command.getAliases())
            {
                if (registeredCommands.containsKey(alias))
                {
                    LOGGER.error("Alias \"" + alias + "\" could not be registered because a command (or alias) with this name already exists!");
                    continue;
                }
                registeredCommands.put(alias, command);
            }
        }
    }

    public List<Command> getRegisteredCommands()
    {
        return registeredCommands.values().stream().distinct().toList();
    }

    /**
     * Returns all commands accessible in a guild
     *
     * @param guildID the id of the guild
     * @return immutable list containing the commands
     */
    public List<Command> getRegisteredCommands(long guildID)
    {
        return registeredCommands
                .values()
                .stream().distinct()
                .filter(x -> x.isAvailableIn(guildID)).toList();
    }

    public List<Command> getGuildCommands(long guildID)
    {
        return registeredCommands
                .values()
                .stream().distinct()
                .filter(x -> x.hasCommandFlag(CommandFlag.PRIVATE_COMMAND) && x.getAllowedGuilds().contains(guildID)).toList();
    }

    @SuppressWarnings("ConstantConditions")
    public void handleCommandFromGuild(@Nonnull GuildMessageReceivedEvent event)
    {
        Runnable r = () ->
        {
            try
            {
                GuildData guildData = GuildManager.getGuildData(event.getGuild());
                CommandArgument arguments = new CommandArgument(event.getMessage().getContentRaw(), guildData.getPrefix());
                String name = arguments.getCommandName();
                if (!registeredCommands.containsKey(name)) return;
                Command command = registeredCommands.get(name);
                if (command.hasCommandFlag(CommandFlag.PRIVATE_COMMAND))
                {
                    if (!command.getAllowedGuilds().contains(event.getGuild().getIdLong())) return;
                }

                if (command.hasCommandFlag(CommandFlag.DEVELOPER_ONLY))
                {
                    if (event.getMember().getIdLong() != Bean.OWNER_ID) return;
                }

                if (command.hasCommandFlag(CommandFlag.DISABLED))
                {
                    if (event.getMember().getIdLong() != Bean.OWNER_ID) return;
                }

                if (command.hasCommandFlag(CommandFlag.MODERATOR_ONLY))
                {
                    if (!guildData.isModerator(event.getMember()))
                    {
                        event.getMessage().reply(LanguageLoader.ofGuild(event.getGuild()).get("general.no_perms", String.class)).mentionRepliedUser(false).queue(s ->
                        {
                        }, ex ->
                        {
                        });
                        return;
                    }
                }
                if (!event.getMember().hasPermission(command.getRequiredPermissions()))
                {
                    event.getMessage().reply(LanguageLoader.ofGuild(event.getGuild()).get("general.no_perms", String.class)).mentionRepliedUser(false).queue(s ->
                    {
                    }, ex ->
                    {
                    });
                    return;
                }
                List<Permission> missingBotPermissions = new ArrayList<>();
                for (Permission p : command.getRequiredBotPermissions())
                {
                    if (!event.getGuild().getSelfMember().hasPermission(p))
                    {
                        missingBotPermissions.add(p);
                    }
                }
                if (missingBotPermissions.size() > 0)
                {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(Color.red)
                            .setFooter(LanguageLoader.ofGuild(event.getGuild()).get("general.no_bot_perms", String.class));
                    StringBuilder sb = new StringBuilder();
                    for (Permission p : missingBotPermissions)
                    {
                        sb.append("`").append(p.getName()).append("`, ");
                    }
                    String toString = sb.toString();
                    toString = toString.substring(0, toString.length() - 2);
                    builder.setDescription(LanguageLoader.ofGuild(event.getGuild()).get("general.no_bot_perms1", String.class) + " \uD83D\uDE26\n" + LanguageLoader.ofGuild(event.getGuild()).get("general.no_bot_perms", String.class) + ": " + toString);
                    event.getChannel().sendMessage(builder.build()).queue();
                    return;
                }
                CommandContext context = new CommandContext(event, arguments, command, event.getMember());
                command.executeCommand(event, context);
            } catch (Exception ex)
            {
                LOGGER.error("An error occured whilst executing command", ex);
                event.getChannel().sendMessage(CommandContext.ERROR_EMOTE + " " + LanguageLoader.ofGuild(event.getGuild()).get("general.unknown_error_occured", String.class)).queue(s ->
                {
                }, exception ->
                {
                });
            }
        };
        Bean.getInstance().getExecutor().submit(r);
    }
}
