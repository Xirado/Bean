package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandContext;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnSlashCommand extends ListenerAdapter
{
    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event)
    {
        if (Bean.getInstance().isDebug() && event.getUser().getIdLong() != Bean.OWNER_ID)
        {
            event.reply(CommandContext.ERROR_EMOTE + " Bot is in debug mode! Only <@"+Bean.OWNER_ID+"> can execute commands!").setEphemeral(true).queue();
            return;
        }
        Bean.getInstance().getSlashCommandHandler().handleSlashCommand(event, event.getMember());
    }
}
