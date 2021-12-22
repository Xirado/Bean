package at.xirado.bean.data;

import net.dv8tion.jda.api.interactions.commands.Command;

public interface IAutocompleteChoice
{
    Command.Choice toCommandAutocompleteChoice();
    String getName();
    String getValue();
}
