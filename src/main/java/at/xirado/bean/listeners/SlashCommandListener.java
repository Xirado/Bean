package at.xirado.bean.listeners;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter
{

    @Override
    public void onSlashCommand(SlashCommandEvent event)
    {
        Guild g = event.getGuild();
        User user = event.getUser();
        if(g == null)
        {
            Bean.getInstance().slashCommandHandler.handleSlashCommand(event, null);
            return;
        }
        g.retrieveMember(user).queue((member) -> Bean.getInstance().slashCommandHandler.handleSlashCommand(event, member), (error) -> {});

    }

}
