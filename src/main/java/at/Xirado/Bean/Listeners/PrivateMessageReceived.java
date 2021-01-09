package at.Xirado.Bean.Listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class PrivateMessageReceived extends ListenerAdapter
{
    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent e)
    {
        Message message = e.getMessage();
        User user = e.getAuthor();
        if(message.isWebhookMessage()) return;
        if(user.isBot()) return;
        if(user.getIdLong() != 184654964122058752L) return;
        if(message.getContentRaw().startsWith("+info"))
        {
            StringBuilder allguilds = new StringBuilder(e.getJDA().getGuilds().size() + " Guilds:\n");
            for(Guild g : e.getJDA().getGuilds())
            {
                allguilds.append(g.getName()).append(" ").append(g.getOwner().getAsMention()).append(" (").append(g.getOwner().getUser().getAsTag()).append(")").append("\n");
            }
            e.getChannel().sendMessage(allguilds.toString().trim()).queue();
        }
    }
}
