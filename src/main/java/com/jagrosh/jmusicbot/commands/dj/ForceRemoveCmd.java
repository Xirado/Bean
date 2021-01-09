// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ForceRemoveCmd extends DJCommand
{
    public ForceRemoveCmd(final Bot bot) {
        super(bot);
        this.name = "forceremove";
        this.help = "removes all entries by a user from the queue";
        this.arguments = "<user>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = false;
        this.bePlaying = true;
        this.botPermissions = new Permission[] { Permission.MESSAGE_EMBED_LINKS };
    }
    
    @Override
    public void doCommand(final CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("You need to mention a user!");
            return;
        }
        final AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getQueue().isEmpty()) {
            event.replyError("There is nothing in the queue!");
            return;
        }
        final List<Member> found = FinderUtil.findMembers(event.getArgs(), event.getGuild());
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
                    .setEventWaiter(this.bot.getWaiter())
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
