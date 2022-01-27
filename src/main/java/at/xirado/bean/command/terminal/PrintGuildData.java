package at.xirado.bean.command.terminal;

import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.log.Shell;

public class PrintGuildData extends ConsoleCommand {

    public PrintGuildData() {
        this.invoke = "printguilddata";
        this.description = "prints the guild data json of a guild";
    }

    @Override
    public void executeCommand(String invoke, String[] args) {
        if (args.length != 1) {
            Shell.printErr("Missing argument \"guild\"!");
            return;
        }
        String guildIDString = args[0];
        long guildID;
        String json;
        try {
            guildID = Long.parseLong(guildIDString);
        } catch (NumberFormatException exception) {
            Shell.printErr("Argument is not a valid guild id!");
            return;
        }

        try {
            json = GuildManager.getGuildDataJSON(guildID);
        } catch (Exception ex) {
            System.out.println("An error occurred!");
            ex.printStackTrace();
            return;
        }

        if (json == null) {
            System.out.println("Could not find data for this guild!");
            return;
        }
        System.out.println(json);
    }
}
