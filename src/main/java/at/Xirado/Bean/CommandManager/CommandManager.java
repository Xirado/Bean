package at.Xirado.Bean.CommandManager;

import at.Xirado.Bean.Commands.*;
import at.Xirado.Bean.Logging.Console;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Modules.GabrielHelp;
import at.Xirado.Bean.Modules.LukasHelp;
import at.Xirado.Bean.Modules.ReneHelp;
import at.Xirado.Bean.Music.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CommandManager
{
    public final ArrayList<Command> registeredCommands = new ArrayList<>();
    public final HashMap<Long, ArrayList<Command>> registeredModules = new HashMap<>();

    public ArrayList<Command> getRegisteredModules(Long guildID)
    {
        if(registeredModules.containsKey(guildID))
        {
            ArrayList<Command> modules = registeredModules.get(guildID);
            return modules == null ? new ArrayList<>() : modules;
        }
        return new ArrayList<>();
    }

    protected void registerModule(Long guildID, Command command)
    {
        ArrayList<Command> commands = getRegisteredModules(guildID);
        commands.add(command);
        registeredModules.put(guildID,commands);
    }

    public void handleCommand(GuildMessageReceivedEvent e)
    {
        Runnable r = () ->
        {
            try
            {
                if(e == null) return;

                CommandArgument arguments = new CommandArgument(e.getMessage().getContentRaw(), e.getGuild().getIdLong());
                String invoke = arguments.getCommand();
                e.getGuild().retrieveMember(e.getAuthor()).queue(
                        (member) ->
                        {
                            if(DiscordBot.debugMode)
                            {
                                if(member.getIdLong() != 184654964122058752L) return;
                            }
                            for(Command cmd : registeredCommands)
                            {

                                if(cmd == null) continue;
                                if(cmd.invoke == null) continue;
                                if(cmd.aliases == null) continue;

                                if(cmd.getInvoke().equalsIgnoreCase(invoke) || Arrays.stream(cmd.getAliases()).anyMatch(invoke::equalsIgnoreCase))
                                {
                                    if(!cmd.isGlobal())
                                    {
                                        if(cmd.enabledGuilds == null) return;
                                        if(!Arrays.asList(cmd.enabledGuilds).contains(e.getGuild().getIdLong()))
                                        {
                                            return;
                                        }
                                    }
                                    if(cmd.getNeededPermissions() == null) return;
                                    Permission[] neededPermissions = cmd.getNeededPermissions();
                                    for(Permission p : neededPermissions)
                                    {
                                        if(!member.hasPermission(p))
                                        {
                                            EmbedBuilder builder = new EmbedBuilder()
                                                    .setColor(Color.red)
                                                    .setTimestamp(Instant.now())
                                                    .setFooter("Insufficient permissions")
                                                    .setDescription("\uD83D\uDEAB You don't have permission to do this! \uD83D\uDEAB")
                                                    .setAuthor(member.getUser().getAsTag(), null, member.getUser().getEffectiveAvatarUrl());
                                            e.getChannel().sendMessage(builder.build()).queue((response) -> response.delete().queueAfter(10, TimeUnit.SECONDS));
                                            return;
                                        }
                                    }
                                    CommandEvent ice = new CommandEvent(arguments, e);
                                    ice.setMember(member);
                                    ice.setCommand(cmd);
                                    cmd.executeCommand(ice);
                                    break;
                                }
                            }
                        }
                );

            }catch(Exception ex)
            {
                Console.error(ExceptionUtils.getStackTrace(ex));
            }
        };
        DiscordBot.instance.scheduledExecutorService.submit(r);
    }
    public void registerAllCommands()
    {
        JDA jda = DiscordBot.instance.jda;
        addCommand(new Announce(jda));
        addCommand(new Avatar(jda));
        addCommand(new Ban(jda));
        addCommand(new Blacklist(jda));
        addCommand(new Clear(jda));
        addCommand(new EditMessage(jda));
        addCommand(new Help(jda));
        addCommand(new Mock(jda));
        addCommand(new PostEmbed(jda));
        addCommand(new ReactionRole(jda));
        addCommand(new Settings(jda));
        addCommand(new Unban(jda));
        addCommand(new Uptime(jda));
        addCommand(new UrbanDictionary(jda));
        addCommand(new PlayCommand(jda));
        addCommand(new PauseCommand(jda));
        addCommand(new StopCommand(jda));
        addCommand(new SkipCommand(jda));
        addCommand(new ForceSkipCommand(jda));
        addCommand(new NowPlayingCommand(jda));
        addCommand(new QueueCommand(jda));
        addCommand(new SkipToCommand(jda));
        addCommand(new ForceRemove(jda));
        addCommand(new MoveTrackCommand(jda));
        addCommand(new GabrielHelp(jda));
        addCommand(new LukasHelp(jda));
        addCommand(new ReneHelp(jda));
        addCommand(new Haste(jda));
        //addCommand(new GameKeys(jda));
        //addCommand(new Warn(jda));
        //addCommand(new Warns(jda));
        //addCommand(new CaseCommand(jda));
        //addCommand(new Tempban(jda));



    }
    protected void addCommand(Command cmd)
    {
        this.registeredCommands.add(cmd);
        if(!cmd.isGlobal())
        {
            for(Long guildID : cmd.enabledGuilds)
            {
                registerModule(guildID,cmd);
            }
        }
    }
}
