package at.xirado.bean.data;

import net.dv8tion.jda.api.interactions.commands.Command;

public class BasicAutocompletionChoice implements IAutocompleteChoice
{
    private final String name, value;

    public BasicAutocompletionChoice(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    @Override
    public Command.Choice toCommandAutocompleteChoice()
    {
        return new Command.Choice(name, value);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getValue()
    {
        return value;
    }
}
