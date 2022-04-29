package at.xirado.bean.backend.routes;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

public class CommandsRoute implements Route
{
    public static final long DEV_GUILD_ID = 815597207617142814L;

    @Override
    public Object handle(Request request, Response response) throws Exception
    {
        List<SlashCommand> commands = Bean.getInstance().isDebug()
                ? Bean.getInstance().getInteractionHandler().getGuildCommands().get(DEV_GUILD_ID)
                .stream().filter(cmd -> cmd instanceof SlashCommand).map(cmd -> (SlashCommand) cmd).toList()
                : Bean.getInstance().getInteractionHandler().getPublicCommands().stream().filter(cmd -> cmd.getType() == Command.Type.SLASH)
                .map(cmd -> (SlashCommand) cmd).toList();
        DataArray commandArray = DataArray.empty();
        for (SlashCommand command : commands)
        {
            DataObject commandObject = DataObject.empty();
            SlashCommandData slashCommandData = (SlashCommandData) command.getCommandData();
            DataArray options = DataArray.empty();
            DataArray subCommands = DataArray.empty();

            for (OptionData option : slashCommandData.getOptions())
            {
                DataObject optionObject = DataObject.empty()
                        .put("name", option.getName())
                        .put("type", option.getType().toString())
                        .put("description", option.getDescription())
                        .put("required", option.isRequired());
                options.add(optionObject);
            }
            for (SubcommandData subcommandData : slashCommandData.getSubcommands())
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
            commandObject.put("name", slashCommandData.getName())
                    .put("description", slashCommandData.getDescription())
                    .put("options", options)
                    .put("sub_commands", subCommands);
            commandArray.add(commandObject);
        }
        return commandArray.toString();
    }
}
