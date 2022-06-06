package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.misc.Metrics;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;


public class MessageCreateListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild())
            return;
        if (GuildJoinListener.isGuildBanned(event.getGuild().getIdLong())) {
            Metrics.MESSAGES.labels("banned").inc();
            return;
        }
        Metrics.MESSAGES.labels("messages").inc();
        if (event.isWebhookMessage() || event.getAuthor().isBot()) return;
        Member member = event.getMember();
        if (member == null) return;
        String content = event.getMessage().getContentRaw();
        GuildData guildData = GuildManager.getGuildData(event.getGuild());
        String prefix = guildData.getPrefix();
        String[] args = content.split("\\s+");
        if (args.length == 1
                && event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser())
                && !event.getMessage().getMentions().mentionsEveryone()
                && event.getMessage().getReferencedMessage() == null) {
            event.getMessage().reply("<a:ping:818580038949273621>")
                    .mentionRepliedUser(false).queue(s ->
                    {
                    }, e ->
                    {
                    });
            return;
        }

        if (content.startsWith(prefix) || content.startsWith("<@" + event.getJDA().getSelfUser().getIdLong() + "> ")) {
            Bean.getInstance().getCommandHandler().handleCommandFromGuild(event);
        }
    }
}
