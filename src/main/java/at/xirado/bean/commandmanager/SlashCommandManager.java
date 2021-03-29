package at.xirado.bean.commandmanager;


import at.xirado.bean.commands.slashcommands.BanCommand;
import at.xirado.bean.commands.slashcommands.Choose;
import at.xirado.bean.main.DiscordBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.commands.CommandHook;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Commandmanager for slash-commands
 */
public class SlashCommandManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandManager.class);

    public final List<SlashCommand> registeredCommands;
    public final ConcurrentHashMap<Long, List<SlashCommand>> registeredGuildCommands;
    private final CommandUpdateAction commandUpdateAction;


    public SlashCommandManager()
    {
        registeredCommands = Collections.synchronizedList(new ArrayList<>());
        registeredGuildCommands = new ConcurrentHashMap<>();
        commandUpdateAction = DiscordBot.getInstance().jda.updateCommands();
    }


    public void registerAllCommands()
    {
        registerCommand(new Choose());
        registerCommand(new BanCommand());
        commandUpdateAction.queue();
    }

    private void registerCommand(SlashCommand command)
    {
        if(!command.isGlobal())
        {
            if(command.getEnabledGuilds() == null) return;
            if(command.getEnabledGuilds().isEmpty()) return;
            for(Long guildID : command.getEnabledGuilds())
            {
                Guild guild = DiscordBot.getInstance().jda.getGuildById(guildID);
                if(guild == null) continue;
                CommandCreateAction commandCreateAction = guild.upsertCommand(command.getCommandName(), command.getCommandDescription());
                for(Object data : command.getOptions())
                {
                    CommandUpdateAction.OptionData optionData = (CommandUpdateAction.OptionData) data;
                    DataObject dataObject = optionData.toData();
                    commandCreateAction.addOption((String) dataObject.get("name"), (String) dataObject.get("description"), Command.OptionType.fromKey((int) dataObject.get("type")));
                }
                commandCreateAction.queue(
                        s ->
                        {
                            List<SlashCommand> guildCommands = registeredGuildCommands.containsKey(guildID) ? registeredGuildCommands.get(guildID) : new ArrayList<>();
                            guildCommands.add(command);
                            registeredGuildCommands.put(guildID, guildCommands);
                            LOGGER.info("Registered command "+command.getCommandName()+" for guild "+guild.getName());
                        }
                );
            }
            return;
        }
        commandUpdateAction.addCommands(command.getCommandData());
        registeredCommands.add(command);
    }


    public void handleSlashCommand(@NotNull SlashCommandEvent event, @NotNull Member member)
    {
        Runnable r = () ->
        {
            try
            {
                for(SlashCommand cmd : registeredCommands)
                {
                    if(cmd == null) continue;
                    if(cmd.getCommandName() == null) continue;
                    if(cmd.getCommandName().equalsIgnoreCase(event.getName()))
                    {
                        CommandHook hook = event.getHook();
                        List<Permission> neededPermissions = cmd.getNeededUserPermissions();
                        List<Permission> neededBotPermissions = cmd.getNeededBotPermissions();
                        if(neededPermissions != null)
                        {
                            for(Permission permission : neededPermissions)
                            {
                                if(!member.hasPermission(permission))
                                {
                                    event.acknowledge(true)
                                            .flatMap(v -> hook.sendMessage("You don't have permission to do this!"))
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
                                    event.acknowledge(true)
                                            .flatMap(v -> hook.sendMessage("I don't have the required permission to do this!"))
                                            .queue();
                                    return;
                                }
                            }
                        }
                        cmd.executeCommand(event, member, new CommandContext(event));
                    }
                }
            }catch (Exception e)
            {
                LOGGER.error("Could not execute slash-command", e);
            }
        };
        DiscordBot.getInstance().scheduledExecutorService.submit(r);
    }

}
