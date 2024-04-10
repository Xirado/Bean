package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.data.ReactionRole;
import at.xirado.bean.data.database.entity.DiscordGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MessageReactionRemoveListener extends ListenerAdapter {
    @Override
    public void onMessageReactionRemoveAll(MessageReactionRemoveAllEvent event) {
        if (!event.isFromGuild())
            return;

        Guild guild = event.getGuild();
        long messageId = event.getMessageIdLong();

        Bean.getInstance().getVirtualThreadExecutor().submit(() -> {
            DiscordGuild guildData = Bean.getInstance().getRepository()
                    .getGuildRepository().getGuildDataBlocking(guild.getIdLong());

            guildData.removeReactionRoles(messageId);
        });
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!event.isFromGuild())
            return;

        Guild guild = event.getGuild();
        EmojiUnion emoji = event.getEmoji();
        long messageId = event.getMessageIdLong();
        String reaction = emoji.getType() == Emoji.Type.UNICODE ? emoji.getAsReactionCode() : emoji.asCustom().getId();

        Bean.getInstance().getVirtualThreadExecutor().submit(() -> {
            DiscordGuild guildData = Bean.getInstance().getRepository()
                    .getGuildRepository().getGuildDataBlocking(guild.getIdLong());

            ReactionRole reactionRole = guildData.getReactionRole(messageId, reaction);
            if (reactionRole == null) return;

            Role role = event.getGuild().getRoleById(reactionRole.getRoleId());
            if (role == null || !guild.getSelfMember().canInteract(role)) return;

            event.getGuild().removeRoleFromMember(UserSnowflake.fromId(event.getUserId()), role).queue();
        });
    }
}
