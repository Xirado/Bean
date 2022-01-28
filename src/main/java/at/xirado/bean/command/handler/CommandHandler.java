package at.xirado.bean.command.handler;

import at.xirado.bean.Bean;
import at.xirado.bean.command.Command;
import at.xirado.bean.command.CommandArgument;
import at.xirado.bean.command.CommandContext;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.commands.Eval;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.translation.LocaleLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class CommandHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    private final ConcurrentMap<String, Command> registeredCommands = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

    public CommandHandler()
    {
        registerCommand(new Eval());
    }

    private void registerCommand(@NotNull Command command)
    {
        String name = command.getName();

        if (registeredCommands.containsKey(name))
        {
            LOGGER.error("Command \"{}\" could not be registered because a command (or alias) with this name already exists!", name);
            return;
        }

        registeredCommands.put(name, command);

        List<String> commandAliases = command.getAliases();
        for (String alias : commandAliases)
        {
            if (registeredCommands.containsKey(alias))
                LOGGER.error("Alias \"{}\" could not be registered because a command (or alias) with this name already exists!", alias);
            else
            {
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
                .filter(command -> command.isAvailableIn(guildID)).toList();
    }

    public List<Command> getGuildCommands(long guildID)
    {
        return registeredCommands
                .values()
                .stream().distinct()
                .filter(command -> command.hasCommandFlag(CommandFlag.PRIVATE_COMMAND) && command.getAllowedGuilds().contains(guildID)).toList();
    }

    @SuppressWarnings("ConstantConditions")
    public void handleCommandFromGuild(@Nonnull GuildMessageReceivedEvent event)
    {
        Bean.getInstance().getExecutor().submit(() ->
        {
            try
            {
                GuildData guildData = GuildManager.getGuildData(event.getGuild());
                CommandArgument arguments = new CommandArgument(event.getMessage().getContentRaw(), guildData.getPrefix());
                String name = arguments.getCommandName();

                if (!registeredCommands.containsKey(name))
                    return;

                Command command = registeredCommands.get(name);

                if (command.hasCommandFlag(CommandFlag.PRIVATE_COMMAND))
                {
                    if (!command.isAvailableIn(event.getGuild().getIdLong())) return;
                }

                if (command.hasCommandFlag(CommandFlag.DEVELOPER_ONLY))
                {
                    if (Bean.WHITELISTED_USERS.stream().noneMatch(x -> x == event.getMember().getIdLong()))
                    {
                        event.getMessage().addReaction("❌").queue();
                        return;
                    }
                }

                if (command.hasCommandFlag(CommandFlag.DISABLED))
                {
                    if (!Bean.isWhitelistedUser(event.getAuthor().getIdLong()))
                        return;
                }

                if (command.hasCommandFlag(CommandFlag.MODERATOR_ONLY))
                {
                    if (!guildData.isModerator(event.getMember()))
                    {
                        event.getMessage().reply(LocaleLoader.ofGuild(event.getGuild()).get("general.no_perms", String.class))
                                .mentionRepliedUser(false).queue(message -> {}, ex -> {});
                        return;
                    }
                }

                if (!event.getMember().hasPermission(command.getRequiredPermissions()))
                {
                    event.getMessage().reply(LocaleLoader.ofGuild(event.getGuild()).get("general.no_perms", String.class))
                            .mentionRepliedUser(false).queue(message -> {}, ex -> {});
                    return;
                }

                Member selfMember = event.getGuild().getSelfMember();


                Collection<Permission> missingBotPermissions = command.getRequiredBotPermissions().stream()
                        .filter(permission -> !selfMember.hasPermission(permission))
                        .toList();


                if (!missingBotPermissions.isEmpty())
                {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(Color.red)
                            .setFooter(LocaleLoader.ofGuild(event.getGuild()).get("general.no_bot_perms", String.class));

                    StringBuilder stringBuilder = new StringBuilder();
                    for (Permission p : missingBotPermissions)
                    {
                        stringBuilder.append("`").append(p.getName()).append("`, ");
                    }

                    String formattedMissingPermissions = stringBuilder.toString();
                    formattedMissingPermissions = formattedMissingPermissions.substring(0, formattedMissingPermissions.length() - 2);

                    builder.setDescription(LocaleLoader.ofGuild(event.getGuild()).get("general.no_bot_perms1", String.class) + " \uD83D\uDE26\n" + LocaleLoader.ofGuild(event.getGuild()).get("general.no_bot_perms", String.class) + ": " + formattedMissingPermissions);

                    event.getChannel().sendMessageEmbeds(builder.build()).queue();
                    return;
                }

                CommandContext context = new CommandContext(event, arguments, command, event.getMember());
                command.executeCommand(event, context);
            }
            catch (Exception ex)
            {
                LOGGER.error("An error occurred whilst executing command", ex);
                event.getChannel().sendMessage(CommandContext.ERROR_EMOTE + " " + LocaleLoader.ofGuild(event.getGuild()).get("general.unknown_error_occured", String.class))
                        .queue(message -> {}, exception -> {});
            }
        });
    }
}