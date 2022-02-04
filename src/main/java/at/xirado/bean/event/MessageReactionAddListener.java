package at.xirado.bean.event;

import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.data.ReactionRole;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReactionAddListener extends ListenerAdapter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReactionAddListener.class);

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent e)
    {
        if (!e.isFromGuild())
            return;
        if (GuildJoinListener.isGuildBanned(e.getGuild().getIdLong()))
            return;
        try
        {
            if (e.getMember().getUser().isBot()) return;
            Guild g = e.getGuild();
            long id = e.getMessageIdLong();
            ReactionEmote reactionemote = e.getReactionEmote();
            String reacted = reactionemote.isEmoji() ? reactionemote.getAsReactionCode() : reactionemote.getEmote().getId();
            GuildData data = GuildManager.getGuildData(e.getGuild());
            ReactionRole reactionRole = data.getReactionRoles().stream()
                    .filter(x -> x.getMessageId() == id && x.getEmote().equals(reacted))
                    .findFirst().orElse(null);
            if (reactionRole != null)
            {
                Role role = e.getGuild().getRoleById(reactionRole.getRoleId());
                if (role != null)
                    g.addRoleToMember(e.getMember(), role).queue(s ->
                    {
                    }, ex ->
                    {
                    });
            }
        } catch (Exception e2)
        {
            LOGGER.error("An error occured whilst executing reaction role event!", e2);
        }
    }
}
