package at.xirado.bean.music;

import at.xirado.bean.commandmanager.Command;
import at.xirado.bean.commandmanager.CommandEvent;
import at.xirado.bean.commandmanager.CommandType;
import at.xirado.bean.main.DiscordBot;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ForceRemove extends Command
{

    public ForceRemove(JDA jda)
    {
        super(jda);
        this.invoke = "forceremove";
        this.description = "Removes all songs from the queue that are from a certain member";
        this.usage = "forceremove [@Member]";
        this.commandType = CommandType.MUSIC;
        this.aliases = Arrays.asList("forcedelete");
    }

    @Override
    public void executeCommand(CommandEvent event) {
        Bot bot = DiscordBot.instance.musicinstance;
        String[] args = event.getArguments().toStringArray();
        String whole = event.getArguments().getAsString(0);
        Guild g = event.getGuild();
        if(!event.isDJ())
        {
            event.replyError("You need to be a DJ to do this!");
            return;
        }
        if (args.length == 0) {
            event.replyError("You need to mention a user!");
            return;
        }
        final AudioHandler handler = ResultHandler.getHandler(g);
        if (handler.getQueue().isEmpty()) {
            event.replyError("There is nothing in the queue!");
            return;
        }
        final List<Member> found = FinderUtil.findMembers(whole, event.getGuild());
        if (found.isEmpty()) {
            event.replyError("Unable to find the user!");
            return;
        }
        if (found.size() > 1) {
            final OrderedMenu.Builder builder = new OrderedMenu.Builder();
            for (int j = 0; j < found.size() && j < 4; ++j) {
                final Member member = found.get(j);
                builder.addChoice("**" + member.getUser().getName() + "**#" + member.getUser().getDiscriminator());
            }

            builder.setSelection(
                    (msg, i) ->
                            this.removeAllEntries(found.get(i - 1).getUser(), event))
                    .setText("Found multiple users:")
                    .setColor(event.getSelfMember().getColor()).useNumbers()
                    .setUsers(event.getAuthor())
                    .useCancelButton(true)
                    .setCancel(msg -> {})
                    .setEventWaiter(bot.getWaiter())
                    .setTimeout(1L, TimeUnit.MINUTES).build().display(event.getChannel());
            return;
        }
        final User target = found.get(0).getUser();
        this.removeAllEntries(target, event);
    }

    private void removeAllEntries(final User target, final CommandEvent event) {
        final int count = ((AudioHandler)event.getGuild().getAudioManager().getSendingHandler()).getQueue().removeAll(target.getIdLong());
        if (count == 0) {
            event.replyWarning("**" + target.getName() + "** doesn't have any songs in the queue!");
        }
        else {
            event.replySuccess("Successfully removed `" + count + "` entries from **" + target.getName() + "**#" + target.getDiscriminator() + ".");
        }
    }
}
