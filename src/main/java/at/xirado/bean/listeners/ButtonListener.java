package at.xirado.bean.listeners;

import at.xirado.bean.misc.AES;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ButtonListener extends ListenerAdapter
{
    public static final AES aes = new AES();

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event)
    {
        if(event.getGuild() == null) return;
        boolean ephemeral = event.getMessage() == null;
        String id = "";
        try
        {
            id = aes.decrypt(event.getComponentId());
        }catch (Exception ex)
        {
            System.out.println("Could not decrypt!");
            if(ephemeral)
            {
                event.editMessage("Could not decrypt message id!\nThis happens after the bot gets restarted and generates a new secret key").setActionRows(Collections.emptyList()).queue();
                return;
            }
        }
        System.out.println(id);
        String[] args = id.split(":");
        switch (args[0])
        {
            case "leave":
                event.deferEdit().queue();
                event.getGuild().kick(event.getMember().getId(), "Didn't agree to server rules").queue();
                break;
            case "accept":
                event.deferEdit().queue();
                Util.sendPM(event.getUser(), "Thanks for accepting the rules!");
                break;
            default:
                System.out.println("Unknown interaction!");
        }
    }
}
