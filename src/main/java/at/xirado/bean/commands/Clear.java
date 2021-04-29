package at.xirado.bean.commands;

import at.xirado.bean.commandutil.CommandCategory;
import at.xirado.bean.commandutil.CommandContext;
import at.xirado.bean.objects.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Clear extends Command
{

    public Clear()
    {
        super("clear", "Bulk-delete messages", "clear [Amount]");
        setCommandCategory(CommandCategory.MODERATION);
        setRequiredPermissions(Permission.MESSAGE_MANAGE);
        setRequiredBotPermissions(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        String[] args = context.getArguments().toStringArray();
        TextChannel channel = event.getChannel();

        if(args.length < 1)
        {
            context.replyErrorUsage();
            return;
        }
        String arg0 = args[0];
        int amount;
        try {
            amount = Integer.parseInt(arg0);
        } catch (NumberFormatException e) {
            context.replyErrorUsage();
            return;
        }
        final OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);
        if (amount < 2) {
            return;
        }
        if (amount > 100) {
            amount = 100;
        }
        final int amount2 = amount;
        event.getMessage().delete().queue(
                (then) ->
                {

                    channel.getHistory().retrievePast(amount2).queue(
                            (hist) -> {
                                if (!hist.isEmpty()) {
                                    List<Message> history = new ArrayList<Message>(hist);
                                    history.removeIf((a) -> a.getTimeCreated().isBefore(twoWeeksAgo));
                                    if (history.size() < 2) {
                                        return;
                                    }
                                    if (history.size() > 100) {
                                        history = history.subList(0,100);
                                    }
                                    final int amount1 = history.size();
                                    channel.deleteMessages(history).queue(
                                            (success) -> {
                                                EmbedBuilder builder = new EmbedBuilder()
                                                        .setColor(Color.green)
                                                        .setDescription(context.getLocalized("commands.message_clear", String.valueOf(amount1)));
                                                channel.sendMessage(builder.build()).queue(response -> response.delete().queueAfter(2, TimeUnit.SECONDS));

                                            }, null
                                    );
                                }
                            },
                            new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE)
                    );
                },
                new ErrorHandler()
                    .ignore(ErrorResponse.UNKNOWN_MESSAGE)
        );
    }
}
