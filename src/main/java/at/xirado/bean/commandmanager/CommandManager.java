package at.xirado.bean.commandmanager;

import at.xirado.bean.commands.moderation.BanCommand;
import at.xirado.bean.commands.moderation.KickCommand;
import at.xirado.bean.translation.I18n;
import at.xirado.bean.main.DiscordBot;
import at.xirado.bean.modules.GabrielHelp;
import at.xirado.bean.modules.LukasHelp;
import at.xirado.bean.modules.ReneHelp;
import at.xirado.bean.commands.*;
import at.xirado.bean.commands.moderation.*;
import at.xirado.bean.music.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);
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

    public void handleCommand(GuildMessageReceivedEvent e, Member member)
    {
        Runnable r = () ->
        {
            try
            {
                if(e == null) return;
                CommandArgument arguments = new CommandArgument(e.getMessage().getContentRaw(), e.getGuild().getIdLong());
                String invoke = arguments.getCommand();
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
                            if(!cmd.getEnabledGuilds().contains(e.getGuild().getIdLong())) return;
                        }
                        if(cmd.getNeededPermissions() == null) return;
                        List<Permission> neededPermissions = cmd.getNeededPermissions();
                        List<Permission> neededBotPermissions = cmd.getNeededBotPermissions();
                        if(e.getAuthor().getIdLong() != DiscordBot.OWNER_ID)
                        {
                            if(!member.hasPermission(e.getChannel(), neededPermissions))
                            {
                                e.getMessage().reply(I18n.ofGuild(e.getGuild()).get("general.no_perms")).mentionRepliedUser(false).queue(s -> {}, ex -> {});
                                return;
                            }
                        }
                        Member botMember = e.getGuild().getMember(DiscordBot.getInstance().jda.getSelfUser());
                        if(!botMember.hasPermission(e.getChannel(), Permission.MESSAGE_WRITE))
                        {
                            LOGGER.warn("Received command \""+arguments.getCommand()+" "+arguments.toString(0)+"\" in "+e.getGuild().getName()+" but can't write messages. Sucks to be them...");
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
                                        .setFooter(I18n.ofGuild(e.getGuild()).get("general.no_bot_perms"));
                                StringBuilder sb = new StringBuilder();
                                for(Permission p : missingPermissions)
                                {
                                    sb.append("`").append(p.getName()).append("`, ");
                                }
                                String toString = sb.toString();
                                toString = toString.substring(0, toString.length()-2);
                                builder.setDescription(I18n.ofGuild(e.getGuild()).get("general.no_bot_perms1")+" \uD83D\uDE26\n"+ I18n.ofGuild(e.getGuild()).get("general.no_bot_perms")+": "+toString);
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

            }catch(Exception ex)
            {
                e.getChannel().sendMessage(I18n.ofGuild(e.getGuild()).get("general.unknown_error_occured")).queue(s -> {}, exception -> {});
            }
        };
        DiscordBot.instance.scheduledExecutorService.submit(r);
    }
    public void registerAllCommands()
    {
        JDA jda = DiscordBot.instance.jda;
        addCommand(new Announce(jda));
        addCommand(new Avatar(jda));
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
        addCommand(new LanguageTest(jda));
    }
    private void addCommand(Command cmd)
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
