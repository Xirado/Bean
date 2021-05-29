package at.xirado.bean.handlers;


import at.xirado.bean.Bean;
import at.xirado.bean.commands.slashcommands.*;
import at.xirado.bean.commandutil.SlashCommandContext;
import at.xirado.bean.objects.SlashCommand;
import at.xirado.bean.translation.LanguageLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Commandmanager for slash-commands
 */
public class SlashCommandHandler
{

    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandHandler.class);

    public final List<SlashCommand> registeredCommands;
    public final ConcurrentHashMap<Long, List<SlashCommand>> registeredGuildCommands;
    private final CommandUpdateAction commandUpdateAction;


    public SlashCommandHandler()
    {
        registeredCommands = Collections.synchronizedList(new ArrayList<>());
        registeredGuildCommands = new ConcurrentHashMap<>();
        commandUpdateAction = Bean.getInstance().jda.updateCommands();
    }


    public void registerAllCommands()
    {
        registerCommand(new Choose());
        registerCommand(new BanCommand());
        registerCommand(new Unban());
        registerCommand(new WarnCommand());
        registerCommand(new CaseCommand());
        registerCommand(new TempbanCommand());
        registerCommand(new UrbanDictionary());
        registerCommand(new Mock());
        registerCommand(new ReactionRole());
        registerCommand(new Avatar());
        registerCommand(new TestCommand());
        registerCommand(new ModlogCommand());
        registerCommand(new RandomFactCommand());
        registerCommand(new JokeCommand());

        queueToDiscord();
    }

    private void queueToDiscord()
    {
        if(!Bean.debugMode)
        {
            commandUpdateAction.queue();
        }else {
            Bean.getInstance().jda.updateCommands().queue();
        }
        for(Map.Entry<Long, List<SlashCommand>> entrySet : registeredGuildCommands.entrySet())
        {
            Long guildID = entrySet.getKey();
            List<SlashCommand> slashCommands = entrySet.getValue();

            if(guildID == null || slashCommands == null) continue;
            if(slashCommands.isEmpty()) continue;
            Guild guild = Bean.getInstance().jda.getGuildById(guildID);
            if(guild == null) continue;
            CommandUpdateAction guildCommandUpdateAction = guild.updateCommands();
            boolean shouldQueue = false;
            for(SlashCommand cmd : slashCommands)
            {
                guildCommandUpdateAction = guildCommandUpdateAction.addCommands(cmd.getCommandData());
                if(!shouldQueue) shouldQueue = true;
            }
            if(shouldQueue)  guildCommandUpdateAction.queue();
        }
    }


    private void registerCommand(SlashCommand command)
    {
        if(!command.isGlobal())
        {
            if(command.getEnabledGuilds() == null) return;
            if(command.getEnabledGuilds().isEmpty()) return;
            for(Long guildID : command.getEnabledGuilds())
            {
                Guild guild = Bean.getInstance().jda.getGuildById(guildID);
                if(guild == null) continue;
                List<SlashCommand> alreadyRegistered = registeredGuildCommands.containsKey(guildID) ? registeredGuildCommands.get(guildID) : new ArrayList<>();
                alreadyRegistered.add(command);
                if(registeredGuildCommands.containsKey(guildID))
                {
                    registeredGuildCommands.replace(guildID, alreadyRegistered);
                }else
                {
                    registeredGuildCommands.put(guildID, alreadyRegistered);
                }
            }
            return;
        }
        if(Bean.debugMode)
        {
            long testServerID = 815597207617142814L;
            Guild guild = Bean.getInstance().jda.getGuildById(testServerID);
            if(guild != null)
            {
                List<SlashCommand> alreadyRegistered = registeredGuildCommands.containsKey(testServerID) ? registeredGuildCommands.get(testServerID) : new ArrayList<>();
                alreadyRegistered.add(command);
                if(registeredGuildCommands.containsKey(testServerID))
                {
                    registeredGuildCommands.replace(testServerID, alreadyRegistered);
                }else
                {
                    registeredGuildCommands.put(testServerID, alreadyRegistered);
                }
            }
            return;
        }
        commandUpdateAction.addCommands(command.getCommandData());
        registeredCommands.add(command);
    }


    public void handleSlashCommand(@NotNull SlashCommandEvent event, @Nullable Member member)
    {
        Runnable r = () ->
        {
            boolean foundCommand = false;
            try
            {
                if(event.getGuild() != null)
                {
                    Guild guild = event.getGuild();
                    long guildID = guild.getIdLong();
                    if(registeredGuildCommands.containsKey(guildID))
                    {
                        List<SlashCommand> guildOnlySlashcommands = registeredGuildCommands.get(guildID);
                        for(SlashCommand cmd : guildOnlySlashcommands)
                        {
                            if(cmd == null) continue;
                            if(cmd.getCommandName() == null) continue;
                            if(cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            {
                                InteractionHook hook = event.getHook();
                                List<Permission> neededPermissions = cmd.getRequiredUserPermissions();
                                List<Permission> neededBotPermissions = cmd.getRequiredBotPermissions();
                                if(neededPermissions != null)
                                {
                                    for(Permission permission : neededPermissions)
                                    {
                                        if(!member.hasPermission(permission))
                                        {
                                            event.deferReply(true)
                                                    .flatMap(v -> hook.sendMessage(LanguageLoader.ofGuild(guild).get("general.no_perms", String.class)))
                                                    .queue();
                                            return;
                                        }
                                    }
                                }

                                if(neededBotPermissions != null)
                                {
                                    for(Permission permission : neededBotPermissions)
                                    {
                                        if(!event.getGuild().getSelfMember().hasPermission(permission))
                                        {
                                            event.deferReply(true)
                                                    .flatMap(v -> hook.sendMessage(LanguageLoader.ofGuild(guild).get("general.no_bot_perms1", String.class)))
                                                    .queue();
                                            return;
                                        }
                                    }
                                }
                                cmd.executeCommand(event, member, new SlashCommandContext(event));
                                return;
                            }
                        }
                    }
                }
                for(SlashCommand cmd : registeredCommands)
                {
                    if(cmd == null) continue;
                    if(cmd.getCommandName() == null) continue;
                    if(cmd.getCommandName().equalsIgnoreCase(event.getName()))
                    {
                        foundCommand = true;
                        if(member == null && !cmd.isRunnableInDM())
                        {
                            event.reply(String.format(LanguageLoader.getForLanguage("en_US").get("commands.cannot_run_in_dm", String.class), SlashCommandContext.ERROR)).setEphemeral(true).queue();
                            return;
                        }
                        InteractionHook hook = event.getHook();
                        List<Permission> neededPermissions = cmd.getRequiredUserPermissions();
                        List<Permission> neededBotPermissions = cmd.getRequiredBotPermissions();
                        if(member != null)
                        {
                            if(neededPermissions != null)
                            {
                                for(Permission permission : neededPermissions)
                                {
                                    if(!member.hasPermission(permission))
                                    {
                                        event.deferReply(true)
                                                .flatMap(v -> hook.sendMessage(LanguageLoader.ofGuild(event.getGuild()).get("general.no_perms", String.class)))
                                                .queue();
                                        return;
                                    }
                                }
                            }

                            if(neededBotPermissions != null)
                            {
                                for(Permission permission : neededBotPermissions)
                                {
                                    if(!event.getGuild().getSelfMember().hasPermission(permission))
                                    {
                                        event.deferReply(true)
                                                .flatMap(v -> hook.sendMessage(LanguageLoader.ofGuild(event.getGuild()).get("general.no_bot_perms1", String.class)))
                                                .queue();
                                        return;
                                    }
                                }
                            }
                        }

                        cmd.executeCommand(event, member, new SlashCommandContext(event));
                    }
                }
                if(!foundCommand && member != null) event.reply(LanguageLoader.ofGuild(event.getGuild()).get("commands.disabled_or_unknown", String.class)).setEphemeral(true).queue();

            }catch (Exception e)
            {
                LOGGER.error("Could not execute slash-command", e);
            }
        };
        Bean.getInstance().scheduledExecutorService.submit(r);
    }

}
