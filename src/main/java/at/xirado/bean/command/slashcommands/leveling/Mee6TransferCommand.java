package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.mee6.MEE6Queue;
import at.xirado.bean.mee6.MEE6Request;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Mee6TransferCommand extends SlashCommand
{
    public Mee6TransferCommand()
    {
        setCommandData(Commands.slash("mee6transfer", "Transfer-command to migrate MEE6 XP to Bean for all found members."));
        setRequiredUserPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        Guild guild = event.getGuild();
        MEE6Queue queue = Bean.getInstance().getMEE6Queue();
        if (queue.hasPendingRequest(guild.getIdLong()))
        {
            event.replyEmbeds(EmbedUtil.warningEmbed("Hey hey! The migration process of your server is still going on!")).setEphemeral(true).queue();
            return;
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("**WARNING** This migrates all MEE6 XP! **WARNING**\n\n" +
                        "This action cannot be undone.\n" +
                        "**Existing xp will be reset for all found users**\n" +
                        "Are you sure you want to continue?\n\n" +
                        "Disclaimer: On larger servers (Over 10k users), it could take a while until all users are migrated!");

        event.deferReply(true).queue();

        event.getHook().sendMessageEmbeds(builder.build())
                .addActionRow(Button.danger("mee6transfer:" + event.getIdLong(), "Continue").withEmoji(Emoji.fromUnicode("⚠")))
                .setEphemeral(true)
                .queue(
                        (hook) ->
                        {
                            Bean.getInstance().getEventWaiter().waitForEvent(
                                    ButtonInteractionEvent.class,
                                    e ->
                                            e.getComponentId().equals("mee6transfer:" + event.getIdLong()),
                                    e ->
                                    {
                                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                                .setDescription("Thank you! We will notify you via DM when we're finished. (Make sure you have DMs turned on!)")
                                                .setColor(0x452350);

                                        queue.addRequest(new MEE6Request(guild.getIdLong(), event.getUser().getIdLong()));

                                        e.editMessageEmbeds(embedBuilder.build()).setActionRows(Collections.emptyList()).queue();
                                    },
                                    30, TimeUnit.SECONDS,
                                    () ->
                                    {
                                        hook.editMessageEmbeds(new EmbedBuilder()
                                                        .setColor(Color.RED)
                                                        .setDescription("Action timed out! Please try again!")
                                                        .build())
                                                .setActionRow(Collections.emptyList()).queue();
                                    }
                            );
                        }
                );
    }
}
