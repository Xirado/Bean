package at.xirado.bean.command;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public interface GenericCommand
{
    CommandData getData();

    boolean isGlobal();

    List<Long> getEnabledGuilds();
}
