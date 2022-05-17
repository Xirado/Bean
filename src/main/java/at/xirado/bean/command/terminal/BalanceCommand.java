package at.xirado.bean.command.terminal;

import at.xirado.bean.Bean;
import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.log.Shell;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.utils.Helpers;

import java.util.Locale;
import java.util.Optional;

public class BalanceCommand extends ConsoleCommand {
    public BalanceCommand() {
        this.invoke = "balance";
        this.description = "get or modify a members balance";
    }

    @Override
    public void executeCommand(String invoke, String[] args) {
        if (args.length < 1) {
            Shell.printErr("Usage:\n    balance get [guild-id] [user-id]\n    balance set [guild-id] [user-id] [new balance]\n    balance add [guild-id] [user-id] [added amount]");
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "get" -> {
                if (args.length < 3) {
                    Shell.printErr("Usage: balance get [guild-id] [user-id]");
                    return;
                }

                if (!Helpers.isNumeric(args[1])) {
                    Shell.printErr("\"" + args[1] + "\" is not a valid guild id!");
                    return;
                }

                if (!Helpers.isNumeric(args[2])) {
                    Shell.printErr("\"" + args[2] + "\" is not a valid user id!");
                    return;
                }

                long guildId = MiscUtil.parseSnowflake(args[1]);
                long userId = MiscUtil.parseSnowflake(args[2]);
                Guild guild = Bean.getInstance().getShardManager().getGuildById(guildId);
                Optional<GuildData> optGuildData = GuildManager.optGuildData(guildId);
                if (optGuildData.isEmpty()) {
                    Shell.printErr("There is no saved data for this guild!");
                    return;
                }
                GuildData guildData = optGuildData.get();
                long amount = guildData.getBalance(userId);
                Shell.println("The balance of user " + userId + " on guild " + guildId + (guild != null ? " (" + guild.getName() + ")" : " ") + " is: $" + amount);
            }
            case "set" -> {
                if (args.length < 4) {
                    Shell.printErr("Usage: balance set [guild-id] [user-id] [new balance]");
                    return;
                }

                if (!Helpers.isNumeric(args[1])) {
                    Shell.printErr("\"" + args[1] + "\" is not a valid guild id!");
                    return;
                }

                if (!Helpers.isNumeric(args[2])) {
                    Shell.printErr("\"" + args[2] + "\" is not a valid user id!");
                    return;
                }

                if (!Helpers.isNumeric(args[3])) {
                    Shell.printErr("\"" + args[3] + "\" is not a valid integer!");
                    return;
                }

                long guildId = MiscUtil.parseSnowflake(args[1]);
                long userId = MiscUtil.parseSnowflake(args[2]);
                long newBalance = MiscUtil.parseLong(args[3]);
                Optional<GuildData> optGuildData = GuildManager.optGuildData(guildId);
                if (optGuildData.isEmpty()) {
                    Shell.printErr("There is no saved data for this guild!");
                    return;
                }
                GuildData guildData = optGuildData.get();
                long oldBalance = guildData.getBalance(userId);
                guildData.setBalance(userId, newBalance);
                Shell.println("Updated balance for user " + userId + " on guild " + guildId + "! New balance: $" + newBalance + " (Old balance: $" + oldBalance + ")");
            }

            case "add" -> {
                if (args.length < 4) {
                    Shell.printErr("Usage: balance add [guild-id] [user-id] [added amount]");
                    return;
                }

                if (!Helpers.isNumeric(args[1])) {
                    Shell.printErr("\"" + args[1] + "\" is not a valid guild id!");
                    return;
                }

                if (!Helpers.isNumeric(args[2])) {
                    Shell.printErr("\"" + args[2] + "\" is not a valid user id!");
                    return;
                }

                if (!Helpers.isNumeric(args[3])) {
                    Shell.printErr("\"" + args[3] + "\" is not a valid integer!");
                    return;
                }

                long guildId = MiscUtil.parseSnowflake(args[1]);
                long userId = MiscUtil.parseSnowflake(args[2]);
                long addedBalance = MiscUtil.parseLong(args[3]);
                Optional<GuildData> optGuildData = GuildManager.optGuildData(guildId);
                if (optGuildData.isEmpty()) {
                    Shell.printErr("There is no saved data for this guild!");
                    return;
                }
                GuildData guildData = optGuildData.get();
                long oldBalance = guildData.getBalance(userId);
                long newBalance = oldBalance + addedBalance;
                guildData.setBalance(userId, oldBalance + addedBalance);
                Shell.println("Updated balance for user " + userId + " on guild " + guildId + "! New balance: $" + newBalance + " (Old balance: $" + oldBalance + ")");
            }
            default -> {
                String arg = args[0].toLowerCase(Locale.ROOT);
                Shell.printErr("Invalid argument \"" + arg + "\"!");
            }
        }
    }
}
