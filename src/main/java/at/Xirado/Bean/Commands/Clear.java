package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
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

    public Clear(JDA jda)
    {
        super(jda);
        this.invoke = "clear";
        this.description = "Bulk-delete messages";
        this.usage = "clear [Amount]";
        this.commandType = CommandType.MODERATION;
        this.neededPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
    }

    @Override
    public void executeCommand(CommandEvent event) {
        String[] args = event.getArguments().getArguments();
        TextChannel channel = event.getChannel();

        if(args.length < 1)
        {
            event.replyErrorUsage();
            return;
        }
        String arg0 = args[0];
        int amount;
        try {
            amount = Integer.parseInt(arg0);
        } catch (NumberFormatException e) {
            event.replyErrorUsage();
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
                                                        .setDescription("Deleted "+amount1+" messages!");
                                                channel.sendMessage(builder.build()).queue(response -> response.delete().queueAfter(5, TimeUnit.SECONDS));
                                                return;

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
