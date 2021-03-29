package at.xirado.bean.listeners;

import at.xirado.bean.main.DiscordBot;
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
        if(g == null) return;
        System.out.println("Guild not null");
        User user = event.getUser();
        System.out.println("Retrieving member");
        g.retrieveMember(user).queue(
                (member) ->
                {
                    System.out.println("Member retrieved");
                    DiscordBot.getInstance().slashCommandManager.handleSlashCommand(event, member);
                    System.out.println("Called handleSlashCommand()");
                },
                (error) ->
                {

                }
        );

    }

}
