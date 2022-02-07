package at.xirado.bean.command.handler;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.GenericCommand;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.command.context.MessageContextCommand;
import at.xirado.bean.command.context.UserContextCommand;
import at.xirado.bean.command.context.message.MockContextMenuCommand;
import at.xirado.bean.command.context.user.SlapContextMenuCommand;
import at.xirado.bean.command.slashcommands.*;
import at.xirado.bean.command.slashcommands.leveling.*;
import at.xirado.bean.command.slashcommands.moderation.*;
import at.xirado.bean.command.slashcommands.music.*;
import at.xirado.bean.data.LinkedDataObject;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.Metrics;
import at.xirado.bean.misc.Util;
import at.xirado.bean.translation.LocaleLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class InteractionCommandHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionCommandHandler.class);

    private final List<GenericCommand> registeredCommands;

    private final ConcurrentHashMap<Long, List<GenericCommand>> registeredGuildCommands;
    private CommandListUpdateAction commandUpdateAction;

    public InteractionCommandHandler()
    {
        registeredCommands = Collections.synchronizedList(new ArrayList<>());
        registeredGuildCommands = new ConcurrentHashMap<>();
    }

    public void initialize()
    {
        commandUpdateAction = Bean.getInstance().getShardManager().getShards().get(0).updateCommands();
        registerAllCommands();
    }

    public void registerAllCommands()
    {
        registerCommand(new BanCommand());
        registerCommand(new UnbanCommand());
        registerCommand(new KickCommand());
        registerCommand(new SoftbanCommand());
        registerCommand(new ModeratorCommand());
        registerCommand(new ReactionRoleCommand());
        registerCommand(new RankCommand());
        registerCommand(new Mee6TransferCommand());
        registerCommand(new LeaderboardCommand());
        registerCommand(new SetXPBackgroundCommand());
        registerCommand(new XPAlertCommand());
        registerCommand(new XPRoleRewardCommand());
        registerCommand(new VoiceGameCommand());
        registerCommand(new PlayCommand());
        registerCommand(new VolumeCommand());
        registerCommand(new SkipCommand());
        registerCommand(new QueueCommand());
        registerCommand(new PlayerCommand());
        registerCommand(new StopCommand());
        registerCommand(new ClearCommand());
        registerCommand(new JoinCommand());
        registerCommand(new DJCommand());
        registerCommand(new PauseCommand());
        registerCommand(new ResumeCommand());
        registerCommand(new RepeatCommand());
        registerCommand(new VoteSkipCommand());
        registerCommand(new SkipToCommand());
        registerCommand(new BookmarkCommand());
        registerCommand(new LaTeXCommand());
        registerCommand(new UrbanDictionaryCommand());
        registerCommand(new AvatarCommand());
        registerCommand(new ChooseCommand());
        registerCommand(new RandomFactCommand());
        registerCommand(new MockCommand());
        registerCommand(new InfoCommand());
        registerCommand(new TestCommand());
        registerCommand(new SlapCommand());
        registerCommand(new RedditCommand());

        // Context commands
        registerCommand(new MockContextMenuCommand());
        registerCommand(new SlapContextMenuCommand());
    }

    public void updateCommands(Consumer<List<Command>> success, Consumer<Throwable> failure)
    {
        if (!Bean.getInstance().isDebug())
        {
            commandUpdateAction.queue(success, failure);
            for (Map.Entry<Long, List<GenericCommand>> entrySet : registeredGuildCommands.entrySet())
            {
                Long guildID = entrySet.getKey();
                List<GenericCommand> slashCommands = entrySet.getValue();
                if (guildID == null || slashCommands == null) continue;
                if (slashCommands.isEmpty()) continue;
                Guild guild = Bean.getInstance().getShardManager().getGuildById(guildID);
                if (guild == null) continue;
                CommandListUpdateAction guildCommandUpdateAction = guild.updateCommands();
                for (GenericCommand cmd : slashCommands)
                {
                    guildCommandUpdateAction = guildCommandUpdateAction.addCommands(cmd.getData());
                }
                if (slashCommands.size() > 0) guildCommandUpdateAction.queue();
            }
        }
        else
        {
            List<GenericCommand> commands = registeredGuildCommands.get(Bean.TEST_SERVER_ID);
            if (commands != null && !commands.isEmpty())
            {
                Guild guild = Bean.getInstance().getShardManager().getGuildById(Bean.TEST_SERVER_ID);
                if (guild == null)
                    return;
                CommandListUpdateAction commandListUpdateAction = guild.updateCommands();
                for (GenericCommand cmd : commands)
                    commandListUpdateAction.addCommands(cmd.getData());
                commandListUpdateAction.queue(success, failure);
            }
        }

    }

    private void registerCommand(GenericCommand command)
    {
        if (!command.isGlobal() && !Bean.getInstance().isDebug())
        {
            if (command.getEnabledGuilds() == null || command.getEnabledGuilds().isEmpty()) return;
            for (Long guildID : command.getEnabledGuilds())
            {
                Guild guild = Bean.getInstance().getShardManager().getGuildById(guildID);
                if (guild == null) continue;
                List<GenericCommand> alreadyRegistered = registeredGuildCommands.containsKey(guildID) ? registeredGuildCommands.get(guildID) : new ArrayList<>();
                alreadyRegistered.add(command);
                registeredGuildCommands.put(guildID, alreadyRegistered);
            }
            return;
        }
        if (Bean.getInstance().isDebug())
        {
            Guild guild = Bean.getInstance().getShardManager().getGuildById(Bean.TEST_SERVER_ID);
            if (guild != null)
            {
                List<GenericCommand> alreadyRegistered = registeredGuildCommands.containsKey(Bean.TEST_SERVER_ID) ? registeredGuildCommands.get(Bean.TEST_SERVER_ID) : new ArrayList<>();
                alreadyRegistered.add(command);
                registeredGuildCommands.put(Bean.TEST_SERVER_ID, alreadyRegistered);
            }
            return;
        }
        commandUpdateAction.addCommands(command.getData());
        registeredCommands.add(command);
    }


    public void handleAutocomplete(@NotNull CommandAutoCompleteInteractionEvent event)
    {
        if (event.getGuild() == null)
            return;
        Runnable r = () ->
        {
            try
            {
                SlashCommand command = null;
                long guildId = event.getGuild().getIdLong();
                if (registeredGuildCommands.containsKey(guildId))
                {
                    List<SlashCommand> guildCommands = registeredGuildCommands.get(guildId)
                            .stream()
                            .filter(cmd -> cmd instanceof SlashCommand)
                            .map(cmd -> (SlashCommand) cmd)
                            .toList();
                    SlashCommand guildCommand = guildCommands.stream().filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (guildCommand != null)
                        command = guildCommand;
                }
                if (command == null)
                {
                    SlashCommand globalCommand = registeredCommands.stream()
                            .filter(cmd -> cmd instanceof SlashCommand)
                            .map(cmd -> (SlashCommand) cmd)
                            .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (globalCommand != null)
                        command = globalCommand;
                }
                if (command != null)
                    command.handleAutocomplete(event);
            }
            catch (Exception ex)
            {
                LOGGER.warn("An error occurred while handling autocomplete!", ex);
                event.replyChoices(Collections.emptyList()).queue(s ->
                {
                }, e ->
                {
                });
            }
        };

        Bean.getInstance().getCommandExecutor().execute(r);
    }

    public void handleMessageContextCommand(@NotNull MessageContextInteractionEvent event)
    {
        if (!event.isFromGuild())
            return;
        Guild guild = event.getGuild();
        Member member = event.getMember();
        MessageContextCommand command = null;

        if (registeredGuildCommands.containsKey(guild.getIdLong()))
        {
            List<MessageContextCommand> guildCommands = registeredGuildCommands.get(guild.getIdLong())
                    .stream()
                    .filter(cmd -> cmd instanceof MessageContextCommand)
                    .map(cmd -> (MessageContextCommand) cmd)
                    .toList();

            MessageContextCommand guildCommand = guildCommands.stream().filter(cmd -> cmd.getData().getName().equalsIgnoreCase(event.getName()))
                    .findFirst()
                    .orElse(null);
            if (guildCommand != null)
                command = guildCommand;
        }

        if (command == null)
        {
            MessageContextCommand globalCommand = getRegisteredMessageContextCommands()
                    .stream()
                    .filter(cmd -> cmd.getData().getName().equalsIgnoreCase(event.getName()))
                    .findFirst()
                    .orElse(null);

            if (globalCommand != null)
                command = globalCommand;
        }

        if (command == null)
            return;

        List<Permission> neededPermissions = command.getRequiredUserPermissions();
        List<Permission> neededBotPermissions = command.getRequiredBotPermissions();
        if (neededPermissions != null && !member.hasPermission((GuildChannel) event.getChannel(), neededPermissions))
        {
            event.reply(LocaleLoader.ofGuild(guild).get("general.no_perms", String.class)).queue();
            return;
        }

        if (neededBotPermissions != null && !event.getGuild().getSelfMember().hasPermission((GuildChannel) event.getChannel(), neededBotPermissions))
        {
            event.reply(LocaleLoader.ofGuild(guild).get("general.no_bot_perms1", String.class)).queue();
            return;
        }

        if (command.getCommandFlags().contains(CommandFlag.MUST_BE_IN_VC))
        {
            GuildVoiceState guildVoiceState = member.getVoiceState();
            if (guildVoiceState == null || !guildVoiceState.inAudioChannel())
            {
                event.replyEmbeds(EmbedUtil.errorEmbed("You are not connected to a voice-channel!")).queue();
                return;
            }
        }

        if (command.getCommandFlags().contains(CommandFlag.MUST_BE_IN_SAME_VC))
        {
            GuildVoiceState voiceState = member.getVoiceState();
            AudioManager manager = event.getGuild().getAudioManager();
            if (manager.isConnected())
            {
                if (!manager.getConnectedChannel().equals(voiceState.getChannel()))
                {
                    event.replyEmbeds(EmbedUtil.errorEmbed("You must be listening in " + manager.getConnectedChannel().getAsMention() + "to do this!")).setEphemeral(true).queue();
                    return;
                }
            }
        }

        MessageContextCommand finalCommand = command;
        Runnable r = () ->
        {
            try
            {
                finalCommand.executeCommand(event);
                Metrics.COMMANDS.labels("success").inc();
            }
            catch (Exception e)
            {
                Metrics.COMMANDS.labels("failed").inc();
                LinkedDataObject translation = event.getGuild() == null ? LocaleLoader.getForLanguage("en_US") : LocaleLoader.ofGuild(event.getGuild());
                if (event.isAcknowledged())
                    event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed(translation.getString("general.unknown_error_occured"))).setEphemeral(true).queue(s ->
                    {
                    }, ex ->
                    {
                    });
                else
                    event.replyEmbeds(EmbedUtil.errorEmbed(translation.getString("general.unknown_error_occured"))).setEphemeral(true).queue(s ->
                    {
                    }, ex ->
                    {
                    });
                LOGGER.error("Could not execute message-context-menu-command", e);
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("An error occurred while executing a message-context-command!")
                        .addField("Guild", event.getGuild() == null ? "None (Direct message)" : event.getGuild().getIdLong() + " (" + event.getGuild().getName() + ")", true)
                        .addField("Channel", event.getGuild() == null ? "None (Direct message)" : event.getChannel().getName(), true)
                        .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + ")", true)
                        .addField("Command", event.getName(), false)
                        .setColor(EmbedUtil.ERROR_COLOR);
                event.getJDA().openPrivateChannelById(Bean.OWNER_ID)
                        .flatMap(c -> c.sendMessageEmbeds(builder.build()).content("```fix\n" + ExceptionUtils.getStackTrace(e) + "\n```"))
                        .queue();
            }
        };

        Bean.getInstance().getCommandExecutor().submit(r);
    }

    public void handleUserContextCommand(@NotNull UserContextInteractionEvent event)
    {
        if (!event.isFromGuild())
            return;
        Guild guild = event.getGuild();
        Member member = event.getMember();
        UserContextCommand command = null;

        if (registeredGuildCommands.containsKey(guild.getIdLong()))
        {
            List<UserContextCommand> guildCommands = registeredGuildCommands.get(guild.getIdLong())
                    .stream()
                    .filter(cmd -> cmd instanceof UserContextCommand)
                    .map(cmd -> (UserContextCommand) cmd)
                    .toList();

            UserContextCommand guildCommand = guildCommands.stream().filter(cmd -> cmd.getData().getName().equalsIgnoreCase(event.getName()))
                    .findFirst()
                    .orElse(null);
            if (guildCommand != null)
                command = guildCommand;
        }

        if (command == null)
        {
            UserContextCommand globalCommand = getRegisteredUserContextCommands()
                    .stream()
                    .filter(cmd -> cmd.getData().getName().equalsIgnoreCase(event.getName()))
                    .findFirst()
                    .orElse(null);

            if (globalCommand != null)
                command = globalCommand;
        }

        if (command == null)
            return;

        List<Permission> neededPermissions = command.getRequiredUserPermissions();
        List<Permission> neededBotPermissions = command.getRequiredBotPermissions();
        if (neededPermissions != null && !member.hasPermission((GuildChannel) event.getChannel(), neededPermissions))
        {
            event.reply(LocaleLoader.ofGuild(guild).get("general.no_perms", String.class)).queue();
            return;
        }

        if (neededBotPermissions != null && !event.getGuild().getSelfMember().hasPermission((GuildChannel) event.getChannel(), neededBotPermissions))
        {
            event.reply(LocaleLoader.ofGuild(guild).get("general.no_bot_perms1", String.class)).queue();
            return;
        }

        if (command.getCommandFlags().contains(CommandFlag.MUST_BE_IN_VC))
        {
            GuildVoiceState guildVoiceState = member.getVoiceState();
            if (guildVoiceState == null || !guildVoiceState.inAudioChannel())
            {
                event.replyEmbeds(EmbedUtil.errorEmbed("You are not connected to a voice-channel!")).queue();
                return;
            }
        }

        if (command.getCommandFlags().contains(CommandFlag.MUST_BE_IN_SAME_VC))
        {
            GuildVoiceState voiceState = member.getVoiceState();
            AudioManager manager = event.getGuild().getAudioManager();
            if (manager.isConnected())
            {
                if (!manager.getConnectedChannel().equals(voiceState.getChannel()))
                {
                    event.replyEmbeds(EmbedUtil.errorEmbed("You must be listening in " + manager.getConnectedChannel().getAsMention() + "to do this!")).setEphemeral(true).queue();
                    return;
                }
            }
        }

        UserContextCommand finalCommand = command;
        Runnable r = () ->
        {
            try
            {
                finalCommand.executeCommand(event);
                Metrics.COMMANDS.labels("success").inc();
            }
            catch (Exception e)
            {
                Metrics.COMMANDS.labels("failed").inc();
                LinkedDataObject translation = event.getGuild() == null ? LocaleLoader.getForLanguage("en_US") : LocaleLoader.ofGuild(event.getGuild());
                if (event.isAcknowledged())
                    event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed(translation.getString("general.unknown_error_occured"))).setEphemeral(true).queue(s ->
                    {
                    }, ex ->
                    {
                    });
                else
                    event.replyEmbeds(EmbedUtil.errorEmbed(translation.getString("general.unknown_error_occured"))).setEphemeral(true).queue(s ->
                    {
                    }, ex ->
                    {
                    });
                LOGGER.error("Could not execute user-context-menu-command", e);
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("An error occurred while executing a user-context-command!")
                        .addField("Guild", event.getGuild() == null ? "None (Direct message)" : event.getGuild().getIdLong() + " (" + event.getGuild().getName() + ")", true)
                        .addField("Channel", event.getGuild() == null ? "None (Direct message)" : event.getChannel().getName(), true)
                        .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + ")", true)
                        .addField("Command", event.getName(), false)
                        .setColor(EmbedUtil.ERROR_COLOR);
                event.getJDA().openPrivateChannelById(Bean.OWNER_ID)
                        .flatMap(c -> c.sendMessageEmbeds(builder.build()).content("```fix\n" + ExceptionUtils.getStackTrace(e) + "\n```"))
                        .queue();
            }
        };

        Bean.getInstance().getCommandExecutor().submit(r);
    }

    public void handleSlashCommand(@NotNull SlashCommandInteractionEvent event, @Nullable Member member)
    {
        Runnable r = () ->
        {
            try
            {
                if (!event.isFromGuild())
                    return;
                Guild guild = event.getGuild();
                SlashCommand command = null;
                long guildId = event.getGuild().getIdLong();

                if (registeredGuildCommands.containsKey(guildId))
                {
                    List<SlashCommand> guildCommands = registeredGuildCommands.get(guildId)
                            .stream()
                            .filter(cmd -> cmd instanceof SlashCommand)
                            .map(cmd -> (SlashCommand) cmd)
                            .toList();
                    SlashCommand guildCommand = guildCommands.stream().filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (guildCommand != null)
                        command = guildCommand;
                }

                if (command == null)
                {
                    SlashCommand globalCommand = getRegisteredSlashCommands()
                            .stream()
                            .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (globalCommand != null)
                        command = globalCommand;
                }

                if (command != null)
                {
                    SlashCommandContext ctx = new SlashCommandContext(event);
                    List<Permission> neededPermissions = command.getRequiredUserPermissions();
                    List<Permission> neededBotPermissions = command.getRequiredBotPermissions();
                    if (neededPermissions != null && !member.hasPermission((GuildChannel) event.getChannel(), neededPermissions))
                    {
                        event.reply(LocaleLoader.ofGuild(guild).get("general.no_perms", String.class))
                                .queue();
                        return;
                    }

                    if (neededBotPermissions != null && !event.getGuild().getSelfMember().hasPermission((GuildChannel) event.getChannel(), neededBotPermissions))
                    {
                        event.reply(LocaleLoader.ofGuild(guild).get("general.no_bot_perms1", String.class))
                                .queue();
                        return;
                    }

                    if (command.getCommandFlags().contains(CommandFlag.DJ_ONLY))
                    {
                        if (!ctx.getGuildData().isDJ(member))
                        {
                            event.replyEmbeds(EmbedUtil.errorEmbed("You need to be a DJ to do this!")).queue();
                            return;
                        }
                    }

                    if (command.getCommandFlags().contains(CommandFlag.MUST_BE_IN_VC))
                    {
                        GuildVoiceState guildVoiceState = member.getVoiceState();
                        if (guildVoiceState == null || !guildVoiceState.inAudioChannel())
                        {
                            event.replyEmbeds(EmbedUtil.errorEmbed("You are not connected to a voice-channel!")).queue();
                            return;
                        }
                    }

                    if (command.getCommandFlags().contains(CommandFlag.MUST_BE_IN_SAME_VC))
                    {
                        GuildVoiceState voiceState = member.getVoiceState();
                        AudioManager manager = event.getGuild().getAudioManager();
                        if (manager.isConnected())
                        {
                            if (!manager.getConnectedChannel().equals(voiceState.getChannel()))
                            {
                                event.replyEmbeds(EmbedUtil.errorEmbed("You must be listening in " + manager.getConnectedChannel().getAsMention() + "to do this!")).setEphemeral(true).queue();
                                return;
                            }
                        }
                    }

                    if (command.getCommandFlags().contains(CommandFlag.REQUIRES_LAVALINK_NODE))
                    {
                        if (!ctx.isLavalinkNodeAvailable())
                        {
                            event.replyEmbeds(EmbedUtil.errorEmbed("There are currently no voice nodes available!\nIf the issue persists, please leave a message on our support server!"))
                                    .addActionRow(Util.getSupportButton())
                                    .queue();
                            return;
                        }
                    }
                    command.executeCommand(event, ctx);
                    Metrics.COMMANDS.labels("success").inc();
                }

            }
            catch (Exception e)
            {
                Metrics.COMMANDS.labels("failed").inc();
                LinkedDataObject translation = event.getGuild() == null ? LocaleLoader.getForLanguage("en_US") : LocaleLoader.ofGuild(event.getGuild());
                if (event.isAcknowledged())
                    event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed(translation.getString("general.unknown_error_occured"))).setEphemeral(true).queue(s ->
                    {
                    }, ex ->
                    {
                    });
                else
                    event.replyEmbeds(EmbedUtil.errorEmbed(translation.getString("general.unknown_error_occured"))).setEphemeral(true).queue(s ->
                    {
                    }, ex ->
                    {
                    });
                LOGGER.error("Could not execute slash-command", e);
                StringBuilder path = new StringBuilder("/" + event.getCommandPath().replace("/", " "));
                for (OptionMapping option : event.getOptions())
                {
                    path.append(" *").append(option.getName()).append("* : ").append("`").append(option.getAsString()).append("`");
                }
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("An error occurred while executing a slash-command!")
                        .addField("Guild", event.getGuild() == null ? "None (Direct message)" : event.getGuild().getIdLong() + " (" + event.getGuild().getName() + ")", true)
                        .addField("Channel", event.getGuild() == null ? "None (Direct message)" : event.getChannel().getName(), true)
                        .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + ")", true)
                        .addField("Command", path.toString(), false)
                        .setColor(EmbedUtil.ERROR_COLOR);
                event.getJDA().openPrivateChannelById(Bean.OWNER_ID)
                        .flatMap(c -> c.sendMessageEmbeds(builder.build()).content("```fix\n" + ExceptionUtils.getStackTrace(e) + "\n```"))
                        .queue();
            }
        };
        Bean.getInstance().getCommandExecutor().execute(r);
    }

    public List<GenericCommand> getRegisteredCommands()
    {
        return registeredCommands;
    }


    public List<SlashCommand> getRegisteredSlashCommands()
    {
        return registeredCommands.stream()
                .filter(cmd -> cmd instanceof SlashCommand)
                .map(cmd -> (SlashCommand) cmd)
                .toList();
    }

    public List<MessageContextCommand> getRegisteredMessageContextCommands()
    {
        return registeredCommands.stream()
                .filter(cmd -> cmd instanceof MessageContextCommand)
                .map(cmd -> (MessageContextCommand) cmd)
                .toList();
    }

    public List<UserContextCommand> getRegisteredUserContextCommands()
    {
        return registeredCommands.stream()
                .filter(cmd -> cmd instanceof UserContextCommand)
                .map(cmd -> (UserContextCommand) cmd)
                .toList();
    }

    public ConcurrentHashMap<Long, List<GenericCommand>> getRegisteredGuildCommands()
    {
        return registeredGuildCommands;
    }
}
