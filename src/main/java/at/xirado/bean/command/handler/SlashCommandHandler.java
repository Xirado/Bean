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
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
        registerCommand(new StopCommand());
        registerCommand(new DJCommand());
        registerCommand(new PauseCommand());
        registerCommand(new ResumeCommand());
        registerCommand(new RepeatCommand());
        registerCommand(new VoteSkipCommand());
        registerCommand(new SkipToCommand());
        registerCommand(new LaTeXCommand());
        registerCommand(new UrbanDictionaryCommand());
        registerCommand(new AvatarCommand());
        registerCommand(new ChooseCommand());
        registerCommand(new JokeCommand());
        registerCommand(new RandomFactCommand());
        registerCommand(new MockCommand());
        registerCommand(new InfoCommand());
        //registerCommand(new BlackJackCommand());
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
                Guild guild = event.getGuild();
                long startTime = System.currentTimeMillis();
                SlashCommand command = null;
                long guildId = event.getGuild().getIdLong();
                if (registeredGuildCommands.containsKey(guildId))
                {
                    List<SlashCommand> guildCommands = registeredGuildCommands.get(guildId);
                    SlashCommand guildCommand = guildCommands.stream().filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (guildCommand != null)
                        command = guildCommand;
                }
                if (command == null)
                {
                    SlashCommand globalCommand = registeredCommands.stream()
                            .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (globalCommand != null)
                        command = globalCommand;
                }
                if (command != null)
                    command.handleAutocomplete(event);
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
            try
            {
                if (!event.isFromGuild())
                    return;
                Guild guild = event.getGuild();
                SlashCommand command = null;
                long guildId = event.getGuild().getIdLong();
                if (registeredGuildCommands.containsKey(guildId))
                {
                    List<SlashCommand> guildCommands = registeredGuildCommands.get(guildId);
                    SlashCommand guildCommand = guildCommands.stream().filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (guildCommand != null)
                        command = guildCommand;
                }
                if (command == null)
                {
                    SlashCommand globalCommand = registeredCommands.stream()
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
                    if (neededPermissions != null && !member.hasPermission(neededPermissions))
                    {
                        event.reply(LocaleLoader.ofGuild(guild).get("general.no_perms", String.class))
                                .queue();
                        return;
                    }
                    if (neededBotPermissions != null && !event.getGuild().getSelfMember().hasPermission(neededBotPermissions))
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
                            event.replyEmbeds(EmbedUtil.errorEmbed("You are not connected to a VoiceChannel!")).queue();
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
                                event.replyEmbeds(EmbedUtil.errorEmbed("I am already playing music in " + manager.getConnectedChannel().getAsMention() + "!")).setEphemeral(true).queue();
                                return;
                            }
                        }
                    }
                    command.executeCommand(event, member, ctx);
                }

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
