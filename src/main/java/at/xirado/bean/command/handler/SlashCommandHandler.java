package at.xirado.bean.command.handler;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.command.slashcommands.*;
import at.xirado.bean.command.slashcommands.leveling.*;
import at.xirado.bean.command.slashcommands.moderation.*;
import at.xirado.bean.command.slashcommands.music.*;
import at.xirado.bean.data.LinkedDataObject;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.translation.LocaleLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ApplicationCommandAutocompleteEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
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

public class SlashCommandHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandHandler.class);

    public final List<SlashCommand> registeredCommands;
    public final ConcurrentHashMap<Long, List<SlashCommand>> registeredGuildCommands;
    private CommandListUpdateAction commandUpdateAction;

    public SlashCommandHandler()
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
        registerCommand(new DJCommand());
        registerCommand(new PauseCommand());
        registerCommand(new ResumeCommand());
        registerCommand(new RepeatCommand());
        registerCommand(new VoteSkipCommand());
        registerCommand(new SkipToCommand());
        registerCommand(new UrbanDictionaryCommand());
        registerCommand(new AvatarCommand());
        registerCommand(new ChooseCommand());
        registerCommand(new JokeCommand());
        registerCommand(new RandomFactCommand());
        registerCommand(new MockCommand());
        registerCommand(new InfoCommand());
        registerCommand(new TestCommand());

    }

    public void updateCommands(Consumer<List<Command>> success, Consumer<Throwable> failure)
    {
        if (!Bean.getInstance().isDebug())
        {
            commandUpdateAction.queue(success, failure);
            for (Map.Entry<Long, List<SlashCommand>> entrySet : registeredGuildCommands.entrySet())
            {
                Long guildID = entrySet.getKey();
                List<SlashCommand> slashCommands = entrySet.getValue();
                if (guildID == null || slashCommands == null) continue;
                if (slashCommands.isEmpty()) continue;
                Guild guild = Bean.getInstance().getShardManager().getGuildById(guildID);
                if (guild == null) continue;
                CommandListUpdateAction guildCommandUpdateAction = guild.updateCommands();
                for (SlashCommand cmd : slashCommands)
                {
                    guildCommandUpdateAction = guildCommandUpdateAction.addCommands(cmd.getCommandData());
                }
                if (slashCommands.size() > 0) guildCommandUpdateAction.queue();
            }
        } else {
            List<SlashCommand> commands = registeredGuildCommands.get(815597207617142814L);
            if (commands != null && !commands.isEmpty())
            {
                Guild guild = Bean.getInstance().getShardManager().getGuildById(815597207617142814L);
                if (guild == null)
                    return;
                CommandListUpdateAction commandListUpdateAction = guild.updateCommands();
                for (SlashCommand cmd : commands)
                    commandListUpdateAction.addCommands(cmd.getCommandData());
                commandListUpdateAction.queue(success, failure);
            }
        }

    }

    private void registerCommand(SlashCommand command)
    {
        if (!command.isGlobal() && !Bean.getInstance().isDebug())
        {
            if (command.getEnabledGuilds() == null) return;
            if (command.getEnabledGuilds().isEmpty()) return;
            for (Long guildID : command.getEnabledGuilds())
            {
                Guild guild = Bean.getInstance().getShardManager().getGuildById(guildID);
                if (guild == null) continue;
                List<SlashCommand> alreadyRegistered = registeredGuildCommands.containsKey(guildID) ? registeredGuildCommands.get(guildID) : new ArrayList<>();
                alreadyRegistered.add(command);
                if (registeredGuildCommands.containsKey(guildID))
                {
                    registeredGuildCommands.replace(guildID, alreadyRegistered);
                } else
                {
                    registeredGuildCommands.put(guildID, alreadyRegistered);
                }
            }
            return;
        }
        if (Bean.getInstance().isDebug())
        {
            long testServerID = 815597207617142814L;
            Guild guild = Bean.getInstance().getShardManager().getGuildById(testServerID);
            if (guild != null)
            {
                List<SlashCommand> alreadyRegistered = registeredGuildCommands.containsKey(testServerID) ? registeredGuildCommands.get(testServerID) : new ArrayList<>();
                alreadyRegistered.add(command);
                registeredGuildCommands.put(testServerID, alreadyRegistered);
            }
            return;
        }
        commandUpdateAction.addCommands(command.getCommandData());
        registeredCommands.add(command);
    }


    public void handleAutocomplete(@NotNull ApplicationCommandAutocompleteEvent event)
    {
        if (event.getGuild() == null)
            return;
        Runnable r = () ->
        {
            try
            {
                boolean foundCommand = false;
                if (registeredGuildCommands.get(event.getGuild().getIdLong()) != null)
                {
                    for (SlashCommand command : registeredGuildCommands.get(event.getGuild().getIdLong()))
                    {
                        if (command.getCommandData().getName().equalsIgnoreCase(event.getName()))
                        {
                            if (!command.getRequiredUserPermissions().isEmpty() && !event.getMember().hasPermission(command.getRequiredUserPermissions()))
                                return;
                            foundCommand = true;
                            command.handleAutocomplete(event);
                            break;
                        }
                    }
                }
                if (foundCommand) return;
                for (SlashCommand command : registeredCommands)
                {
                    if (command.getCommandData().getName().equalsIgnoreCase(event.getName()))
                    {
                        if (!command.getRequiredUserPermissions().isEmpty() && !event.getMember().hasPermission(command.getRequiredUserPermissions()))
                            return;
                        command.handleAutocomplete(event);
                        break;
                    }
                }
            } catch (Exception ex)
            {
                LOGGER.warn("An error occurred while handling autocomplete!", ex);
                event.deferChoices(Collections.emptyList()).queue();
            }
        };
        Bean.getInstance().getExecutor().submit(r);
    }

    public void handleSlashCommand(@NotNull SlashCommandEvent event, @Nullable Member member)
    {
        Runnable r = () ->
        {
            boolean foundCommand = false;
            try
            {
                if (event.getGuild() != null)
                {
                    Guild guild = event.getGuild();
                    long guildID = guild.getIdLong();
                    if (registeredGuildCommands.containsKey(guildID))
                    {
                        List<SlashCommand> guildOnlySlashcommands = registeredGuildCommands.get(guildID);
                        for (SlashCommand cmd : guildOnlySlashcommands)
                        {
                            if (cmd == null) continue;
                            if (cmd.getCommandName() == null) continue;
                            if (cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            {
                                InteractionHook hook = event.getHook();
                                List<Permission> neededPermissions = cmd.getRequiredUserPermissions();
                                List<Permission> neededBotPermissions = cmd.getRequiredBotPermissions();
                                if (neededPermissions != null && !member.hasPermission(neededPermissions))
                                {
                                    event.deferReply(true)
                                            .flatMap(v -> hook.sendMessage(LocaleLoader.ofGuild(guild).get("general.no_perms", String.class)))
                                            .queue();
                                    return;
                                }
                                if (neededBotPermissions != null && !event.getGuild().getSelfMember().hasPermission(neededBotPermissions))
                                {
                                    event.deferReply(true)
                                            .flatMap(v -> hook.sendMessage(LocaleLoader.ofGuild(guild).get("general.no_bot_perms1", String.class)))
                                            .queue();
                                    return;
                                }
                                SlashCommandContext ctx = new SlashCommandContext(event);
                                if (cmd.getCommandFlags().contains(CommandFlag.DJ_ONLY))
                                {
                                    if (!ctx.getGuildData().isDJ(member))
                                    {
                                        ctx.replyError("You need to be a DJ to do this!").queue();
                                        return;
                                    }
                                }
                                if (cmd.getCommandFlags().contains(CommandFlag.MUST_BE_IN_VC))
                                {
                                    GuildVoiceState guildVoiceState = member.getVoiceState();
                                    if (guildVoiceState == null || !guildVoiceState.inVoiceChannel())
                                    {
                                        ctx.replyError("You are not connected to a VoiceChannel!").queue();
                                        return;
                                    }
                                }

                                if (cmd.getCommandFlags().contains(CommandFlag.MUST_BE_IN_SAME_VC))
                                {
                                    GuildVoiceState voiceState = member.getVoiceState();
                                    AudioManager manager = event.getGuild().getAudioManager();
                                    if (manager.isConnected())
                                    {
                                        if (!manager.getConnectedChannel().equals(voiceState.getChannel()))
                                        {
                                            ctx.replyError("I am already playing music in " + manager.getConnectedChannel().getAsMention() + "!").setEphemeral(true).queue();
                                            return;
                                        }
                                    }
                                }
                                cmd.executeCommand(event, member, ctx);
                                return;
                            }
                        }
                    }
                }
                for (SlashCommand cmd : registeredCommands)
                {
                    if (cmd == null) continue;
                    if (cmd.getCommandName() == null) continue;
                    if (cmd.getCommandName().equalsIgnoreCase(event.getName()))
                    {
                        foundCommand = true;
                        if (member == null && !cmd.isRunnableInDM())
                        {
                            event.reply(String.format(LocaleLoader.getForLanguage("en_US").get("commands.cannot_run_in_dm", String.class), SlashCommandContext.ERROR)).setEphemeral(true).queue();
                            return;
                        }
                        InteractionHook hook = event.getHook();
                        List<Permission> neededPermissions = cmd.getRequiredUserPermissions();
                        List<Permission> neededBotPermissions = cmd.getRequiredBotPermissions();
                        if (member != null)
                        {
                            if (neededPermissions != null && !member.hasPermission(neededPermissions))
                            {
                                event.deferReply(true)
                                        .flatMap(v -> hook.sendMessage(LocaleLoader.ofGuild(event.getGuild()).get("general.no_perms", String.class)))
                                        .queue();
                                return;
                            }
                            if (neededBotPermissions != null && !event.getGuild().getSelfMember().hasPermission(neededBotPermissions))
                            {
                                event.deferReply(true)
                                        .flatMap(v -> hook.sendMessage(LocaleLoader.ofGuild(event.getGuild()).get("general.no_bot_perms1", String.class)))
                                        .queue();
                                return;
                            }
                        }
                        SlashCommandContext ctx = new SlashCommandContext(event);
                        if (cmd.getCommandFlags().contains(CommandFlag.DJ_ONLY))
                        {
                            if (!ctx.getGuildData().isDJ(member))
                            {
                                ctx.replyError("You need to be a DJ to do this!").queue();
                                return;
                            }
                        }
                        if (cmd.getCommandFlags().contains(CommandFlag.MUST_BE_IN_VC))
                        {
                            GuildVoiceState guildVoiceState = member.getVoiceState();
                            if (guildVoiceState == null || !guildVoiceState.inVoiceChannel())
                            {
                                ctx.replyError("You are not connected to a VoiceChannel!").queue();
                                return;
                            }
                        }

                        if (cmd.getCommandFlags().contains(CommandFlag.MUST_BE_IN_SAME_VC))
                        {
                            GuildVoiceState voiceState = member.getVoiceState();
                            AudioManager manager = event.getGuild().getAudioManager();
                            if (manager.isConnected())
                            {
                                if (!manager.getConnectedChannel().equals(voiceState.getChannel()))
                                {
                                    ctx.replyError("I am already playing music in " + manager.getConnectedChannel().getAsMention() + "!").setEphemeral(true).queue();
                                    return;
                                }
                            }
                        }
                        cmd.executeCommand(event, member, ctx);
                    }
                }
                if (!foundCommand && member != null)
                    event.reply(LocaleLoader.ofGuild(event.getGuild()).get("commands.disabled_or_unknown", String.class)).setEphemeral(true).queue();

            } catch (Exception e)
            {
                LOGGER.error("Could not execute slash-command", e);
                StringBuilder path = new StringBuilder("/"+event.getCommandPath().replace("/", " "));
                for(OptionMapping option : event.getOptions())
                {
                    path.append(" *").append(option.getName()).append("* : ").append("`").append(option.getAsString()).append("`");
                }
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("An error occurred while executing a slash-command!")
                        .addField("Guild", event.getGuild() == null ? "None (Direct message)" : event.getGuild().getIdLong()+" ("+event.getGuild().getName()+")",true)
                        .addField("Channel", event.getGuild() == null ? "None (Direct message)" : event.getChannel().getName() , true)
                        .addField("User", event.getUser().getAsMention()+" ("+event.getUser().getAsTag()+")", true)
                        .addField("Command", path.toString(), false)
                        .setColor(EmbedUtil.ERROR_COLOR);
                event.getJDA().openPrivateChannelById(Bean.OWNER_ID)
                        .flatMap(c -> c.sendMessageEmbeds(builder.build()).content("```fix\n"+ExceptionUtils.getStackTrace(e)+"\n```"))
                        .queue();
                LinkedDataObject translation = event.getGuild() == null ? LocaleLoader.getForLanguage("en_US") : LocaleLoader.ofGuild(event.getGuild());
                event.replyEmbeds(EmbedUtil.errorEmbed(translation.getString("general.unknown_error_occured"))).setEphemeral(true).queue(s -> {}, ex -> {});
            }
        };
        Bean.getInstance().getExecutor().submit(r);
    }
}
