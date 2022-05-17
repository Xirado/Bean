package at.xirado.bean.command.terminal;

import at.xirado.bean.Bean;
import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.event.GuildJoinListener;
import at.xirado.bean.log.Shell;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.Helpers;

import java.util.Arrays;

public class BanGuildCommand extends ConsoleCommand {
    public BanGuildCommand() {
        this.invoke = "ban";
        this.description = "Bans a guild (Will not handle events from a banned guild)";
    }

    @Override
    public void executeCommand(String invoke, String[] args) {
        if (args.length < 2) {
            Shell.printErr("You need to provide a guild id and a reason");
            return;
        }

        String guild = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (!Helpers.isNumeric(guild)) {
            Shell.printErr("That's not a valid guild id!");
            return;
        }
        long guildId = Long.parseLong(guild);
        if (GuildJoinListener.isGuildBanned(guildId)) {
            GuildJoinListener.banGuild(guildId, reason);
            Shell.println("Guild already banned, updated reason!");
            return;
        }
        GuildJoinListener.banGuild(guildId, reason);
        Shell.println("Banned guild " + guildId + "!");
        Guild guildObject = Bean.getInstance().getShardManager().getGuildById(guildId);
        if (guildObject != null) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(EmbedUtil.ERROR_COLOR)
                    .setTitle("Your server " + guildObject.getName() + " has been blacklisted!")
                    .addField("Reason", reason, true)
                    .setDescription("No commands will work on this server anymore.\nSend a message to xirado#0001 (" + Bean.OWNER_ID + ") to appeal.")
                    .build();
            Util.sendDM(guildObject.getOwnerIdLong(), embed);
        }
    }
}
