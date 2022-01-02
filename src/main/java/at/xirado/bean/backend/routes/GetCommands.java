package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;

public class GetCommands
{
    public static final long DEV_GUILD_ID = 815597207617142814L;

    public static Object handle(Request request, Response response) throws IOException
    {
        List<SlashCommand> commands = Bean.getInstance().isDebug()
                ? Bean.getInstance().getSlashCommandHandler().getRegisteredGuildCommands().get(DEV_GUILD_ID)
                : Bean.getInstance().getSlashCommandHandler().getRegisteredCommands();
        DataArray commandArray = DataArray.empty();
        for (SlashCommand command : commands)
        {
            CommandData commandData = command.getCommandData();
            DataObject commandObject = DataObject.empty();

            DataArray options = DataArray.empty();
            DataArray subCommands = DataArray.empty();

            for (OptionData option : commandData.getOptions())
            {
                DataObject optionObject = DataObject.empty()
                        .put("name", option.getName())
                        .put("type", option.getType().toString())
                        .put("description", option.getDescription())
                        .put("required", option.isRequired());
                options.add(optionObject);
            }
            for (SubcommandData subcommandData : commandData.getSubcommands())
            {
                DataObject subCommandObject = DataObject.empty();
                DataArray subCommandOptions = DataArray.empty();
                for (OptionData option : subcommandData.getOptions())
                {
                    DataObject optionObject = DataObject.empty()
                            .put("name", option.getName())
                            .put("type", option.getType().toString())
                            .put("description", option.getDescription())
                            .put("required", option.isRequired());
                    subCommandOptions.add(optionObject);
                }
                subCommandObject.put("name", subcommandData.getName())
                        .put("description", subcommandData.getDescription());
                subCommandObject.put("options", subCommandOptions);
                subCommands.add(subCommandObject);
            }
            commandObject.put("name", commandData.getName())
                    .put("description", commandData.getDescription())
                    .put("options", options)
                    .put("sub_commands", subCommands);
            commandArray.add(commandObject);
        }
        return commandArray.toString();
    }
}
