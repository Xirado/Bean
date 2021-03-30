package at.xirado.bean.listeners;

import at.xirado.bean.commandmanager.CommandContext;
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
        if(g == null)
        {
            event.reply(CommandContext.ERROR+" this command can not be accessed via DM!").setEphemeral(true).queue();
            return;
        }
        User user = event.getUser();
        g.retrieveMember(user).queue(
                (member) ->
                {
                    DiscordBot.getInstance().slashCommandManager.handleSlashCommand(event, member);
                },
                (error) ->
                {

                }
        );

    }

}
