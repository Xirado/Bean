package at.xirado.bean.commandmanager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class SlashCommand
{

    private CommandUpdateAction.CommandData commandData;
    private List<Permission> neededUserPermissions;
    private List<Permission> neededBotPermissions;
    private boolean isGlobal;
    private List<Long> enabledGuilds;

    public String getCommandName()
    {
        return (String) this.commandData.toData().get("name");
    }

    public String getCommandDescription()
    {
        return (String) this.commandData.toData().get("description");
    }

    public DataArray getOptions()
    {
        return (DataArray) this.commandData.toData().get("options");
    }


    public CommandUpdateAction.CommandData getCommandData()
    {
        return commandData;
    }

    public void setCommandData(CommandUpdateAction.CommandData commandData)
    {
        this.commandData = commandData;
    }

    public List<Permission> getNeededUserPermissions()
    {
        return neededUserPermissions;
    }

    public void setNeededUserPermissions(List<Permission> neededUserPermissions)
    {
        this.neededUserPermissions = neededUserPermissions;
    }

    public List<Permission> getNeededBotPermissions()
    {
        return neededBotPermissions;
    }

    public void setNeededBotPermissions(List<Permission> neededBotPermissions)
    {
        this.neededBotPermissions = neededBotPermissions;
    }

    public boolean isGlobal()
    {
        return isGlobal;
    }

    public void setGlobal(boolean global)
    {
        isGlobal = global;
    }

    public List<Long> getEnabledGuilds()
    {
        return enabledGuilds;
    }

    public void setEnabledGuilds(List<Long> enabledGuilds)
    {
        this.enabledGuilds = enabledGuilds;
    }



    public SlashCommand()
    {
        this.commandData = null;
        this.isGlobal = true;
        this.enabledGuilds = new ArrayList<>();
    }

    public abstract void executeCommand(@NotNull SlashCommandEvent event, @NotNull Member sender, @NotNull CommandContext ctx);

}
