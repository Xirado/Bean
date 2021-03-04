package at.Xirado.Bean.CommandManager;

import at.Xirado.Bean.Commands.*;
import at.Xirado.Bean.Commands.Moderation.*;
import at.Xirado.Bean.Logging.Console;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Modules.GabrielHelp;
import at.Xirado.Bean.Modules.LukasHelp;
import at.Xirado.Bean.Modules.ReneHelp;
import at.Xirado.Bean.Music.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private void registerModule(Long guildID, Command command)
    {
        ArrayList<Command> commands = getRegisteredModules(guildID);
        if(commands.stream().noneMatch((c) -> c.equals(command)))
        {
            commands.add(command);
            registeredModules.put(guildID,commands);
        }

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

                                if(cmd.getInvoke().equalsIgnoreCase(invoke) || cmd.getAliases().stream().anyMatch(invoke::equalsIgnoreCase))
                                {
                                    if(!cmd.isGlobal())
                                    {
                                        if(cmd.enabledGuilds == null) return;
                                        if(!cmd.getEnabledGuilds().contains(e.getGuild().getIdLong()))
                                        {
                                            return;
                                        }
                                    }
                                    if(cmd.getNeededPermissions() == null) return;
                                    List<Permission> neededPermissions = cmd.getNeededPermissions();
                                    List<Permission> neededBotPermissions = cmd.getNeededBotPermissions();
                                    if(e.getAuthor().getIdLong() != DiscordBot.OWNER_ID)
                                    {
                                        if(!member.hasPermission(e.getChannel(), neededPermissions))
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
                                    Member botMember = e.getGuild().getMember(DiscordBot.getInstance().jda.getSelfUser());
                                    if(!botMember.hasPermission(e.getChannel(), Permission.MESSAGE_WRITE))
                                    {
                                        Console.logger.warn("Received command \""+arguments.getCommand()+" "+arguments.getAsString(0)+"\" in "+e.getGuild().getName()+" but can't write messages. Sucks to be them...");
                                        return;
                                    }
                                    if(neededBotPermissions != null && neededBotPermissions.size() > 0)
                                    {

                                        List<Permission> missingPermissions = new ArrayList<>();
                                        for(Permission p : neededBotPermissions)
                                        {
                                            if(!botMember.hasPermission(p))
                                            {
                                                missingPermissions.add(p);
                                            }
                                        }
                                        if(missingPermissions.size() > 0)
                                        {
                                            EmbedBuilder builder = new EmbedBuilder()
                                                    .setColor(Color.red)
                                                    .setFooter("Missing bot permissions");
                                            StringBuilder sb = new StringBuilder();
                                            for(Permission p : missingPermissions)
                                            {
                                                sb.append("`").append(p.getName()).append("`, ");
                                            }
                                            String toString = sb.toString();
                                            toString = toString.substring(0, toString.length()-2);
                                            builder.setDescription("Oops, it seems as i don't have the permission to do this \uD83D\uDE26\nMissing Permissions: "+toString);
                                            e.getChannel().sendMessage(builder.build()).queue();
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
                        },
                        (error) -> Console.logger.error("Could not retrieve member!", error)
                );

            }catch(Throwable t)
            {
                Console.logger.error("An error occured while executing a command!", t);
            }
        };
        DiscordBot.instance.scheduledExecutorService.submit(r);
    }
    public void registerAllCommands()
    {
        JDA jda = DiscordBot.instance.jda;
        addCommand(new Announce(jda));
        addCommand(new Avatar(jda));
        //addCommand(new Ban(jda));
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

        addCommand(new BanCommand(jda));
        addCommand(new SoftBanCommand(jda));
        addCommand(new KickCommand(jda));
        addCommand(new TempbanCommand(jda));
        addCommand(new CaseCommand(jda));
        addCommand(new AddModeratorCommand(jda));
        addCommand(new RemoveModeratorCommand(jda));
        addCommand(new ListModeratorsCommand(jda));
        addCommand(new ModlogCommand(jda));
        addCommand(new WarnCommand(jda));
        addCommand(new MuteCommand(jda));
        addCommand(new SetMutedRoleCommand(jda));
        addCommand(new UnmuteCommand(jda));



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
