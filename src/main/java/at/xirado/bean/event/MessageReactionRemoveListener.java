package at.xirado.bean.event;

import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReactionRemoveListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReactionRemoveListener.class);

    @Override
    public void onMessageReactionRemoveAll(MessageReactionRemoveAllEvent e) {
        if (!e.isFromGuild())
            return;
        if (GuildJoinListener.isGuildBanned(e.getGuild().getIdLong()))
            return;
        try {
            long messageId = e.getMessageIdLong();
            GuildData data = GuildManager.getGuildData(e.getGuild());
            data.removeReactionRoles(messageId).update();
        } catch (Exception exception) {
            LOGGER.error("An error occured while executing GuildMessageReactionRemoveAllEvent!", exception);
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent e) {
        if (!e.isFromGuild())
            return;
        if (GuildJoinListener.isGuildBanned(e.getGuild().getIdLong()))
            return;
        ReactionEmote reactionemote = e.getReactionEmote();
        String reacted = reactionemote.isEmoji() ? reactionemote.getAsReactionCode() : reactionemote.getEmote().getId();
        GuildData data = GuildManager.getGuildData(e.getGuild());
        data.getReactionRoles().stream()
                .filter(x -> x.getMessageId() == e.getMessageIdLong() && x.getEmote().equals(reacted))
                .findFirst().ifPresent(reactionRole -> e.getGuild().retrieveMemberById(e.getUserId()).queue(
                        (member) ->
                        {
                            if (member.getUser().isBot()) return;
                            Role role = e.getGuild().getRoleById(reactionRole.getRoleId());
                            if (role != null)
                                e.getGuild().removeRoleFromMember(member, role).queue(s ->
                                {
                                }, ex ->
                                {
                                });
                        },
                        (error) ->
                        {
                        }
                ));

    }
}
