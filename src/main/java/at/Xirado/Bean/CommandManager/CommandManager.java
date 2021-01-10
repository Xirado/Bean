package at.Xirado.Bean.CommandManager;

import at.Xirado.Bean.Commands.*;
import at.Xirado.Bean.Main.DiscordBot;
import at.Xirado.Bean.Modules.GabrielHelp;
import at.Xirado.Bean.Modules.GameKeys;
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
import java.util.Arrays;
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

    protected void addRegisteredModule(Long guildID, Command command)
    {
        ArrayList<Command> commands = getRegisteredModules(guildID);
        commands.add(command);
        registeredModules.put(guildID,commands);
    }

    public void handleCommand(GuildMessageReceivedEvent e)
    {
        if(e == null) return;

        CommandArgument arguments = new CommandArgument(e.getMessage().getContentRaw(), e.getGuild().getIdLong());
        String invoke = arguments.getCommand();
        Member m = e.getMember();
        if(DiscordBot.debugMode)
        {
            if(m.getIdLong() != 184654964122058752L) return;
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
                List<Permission> neededPermissions = Arrays.asList(cmd.getNeededPermissions());
                for(Permission p : neededPermissions)
                {
                    if(!m.hasPermission(p))
                    {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(Color.red)
                                .setTimestamp(Instant.now())
                                .setFooter("Insufficient permissions")
                                .setDescription("\uD83D\uDEAB You don't have permission to do this! \uD83D\uDEAB")
                                .setAuthor(m.getUser().getAsTag(), null, m.getUser().getEffectiveAvatarUrl());
                        e.getChannel().sendMessage(builder.build()).queue((response) -> response.delete().queueAfter(10, TimeUnit.SECONDS));
                        return;
                    }
                }
                CommandEvent ice = new CommandEvent(arguments, e);
                ice.setCommand(cmd);
                cmd.execute(ice);
                break;
            }
        }
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
        addCommand(new GameKeys(jda));
        addCommand(new Warn(jda));
        addCommand(new Warns(jda));
        addCommand(new CaseCommand(jda));



    }
    protected void addCommand(Command cmd)
    {
        this.registeredCommands.add(cmd);
        if(!cmd.isGlobal())
        {
            for(Long guildID : cmd.enabledGuilds)
            {
                addRegisteredModule(guildID,cmd);
            }
        }
    }
}
