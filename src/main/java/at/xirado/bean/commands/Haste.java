package at.xirado.bean.commands;

import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.misc.Util;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;

public class Haste extends Command
{
    public Haste()
    {
        super("haste", "Creates a haste and gives you the link", "haste [Text]");
        setAliases("posthaste", "hastebin");
        setCommandCategory(CommandCategory.UTILITIES);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        if(args.length == 0)
        {
            context.replyErrorUsage();
            return;
        }
        context.deleteInvokeMessage();
        String haste = context.getArguments().toString(0);
        String linkresponse = Util.postHaste(haste, false);
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.decode("#93bdca"))
                .setFooter("Hastebin")
                .setTimestamp(Instant.now())
                .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl())
                .setDescription(context.getLocalized("commands.haste_finished", linkresponse));
        context.reply(builder.build());

    }
}
