package at.xirado.bean.command.terminal;


import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.data.GuildManager;

public class PrintGuildData extends ConsoleCommand
{
    public PrintGuildData()
    {
        this.invoke = "printguilddata";
        this.description = "prints the guild data of a guild if it is loaded in cache";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("missing argument guild!");
            return;
        }

        String guildIDString = args[0];
        long guildID;
        String json;
        try
        {
            guildID = Long.parseLong(guildIDString);
            json = GuildManager.getGuildDataJSON(guildID);
        } catch (Exception ex)
        {
            System.out.println("An error occured!");
            ex.printStackTrace();
            return;
        }

        if (json == null)
        {
            System.out.println("The guild-data for this guild is not loaded!");
            return;
        }

        System.out.println(json);


    }
}
