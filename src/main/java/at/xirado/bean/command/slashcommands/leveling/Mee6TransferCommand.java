package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.Bean;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.MEE6Player;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.data.database.Database;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Mee6TransferCommand extends SlashCommand
{
    public Mee6TransferCommand()
    {
        setCommandData(Commands.slash("mee6transfer", "Transfers MEE6 XP to Bean for all members."));
        setRequiredUserPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx)
    {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("This transfers all XP from MEE6 over to Bean.\n" +
                        " (MEE6 levels will not be reset). This action cannot be undone.\n" +
                        "__**Existing xp will be reset for all found users**__\n" +
                        "Are you sure you want to continue?\n");

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
                                                .setDescription("<a:Loading2:800570529647296513> Loading...")
                                                .setColor(0x452350);

                                        e.editMessageEmbeds(embedBuilder.build()).setActionRows(Collections.emptyList()).queue(
                                                hook2 ->
                                                {
                                                    long startTime = System.currentTimeMillis();

                                                    try (Connection connection = Database.getConnectionFromPool())
                                                    {
                                                        if (connection == null)
                                                        {
                                                            EmbedBuilder embedBuilder1 = new EmbedBuilder()
                                                                    .setDescription(ctx.getLocalized("general.db_error"))
                                                                    .setColor(0x452350);

                                                            e.editMessageEmbeds(embedBuilder1.build()).queue();
                                                            return;
                                                        }
                                                        int page = 0;
                                                        int transferredPlayers = 0;
                                                        long guildID = event.getGuild().getIdLong();

                                                        ObjectMapper objectMapper = new ObjectMapper();

                                                        while (true)
                                                        {
                                                            if (System.currentTimeMillis() > startTime + 30000)
                                                            {
                                                                EmbedBuilder embedBuilder2 = new EmbedBuilder()
                                                                        .setDescription("Timed out! Please try again later!")
                                                                        .setColor(0x452350);
                                                                hook2.editOriginalEmbeds(embedBuilder2.build()).queue();
                                                                return;
                                                            }

                                                            JsonNode node = objectMapper.readTree(new URL("https://mee6.xyz/api/plugins/levels/leaderboard/" + guildID + "?page=" + page));
                                                            MEE6Player[] players = objectMapper.treeToValue(node.at("/players"), MEE6Player[].class);

                                                            if (players == null || players.length == 0)
                                                                break;

                                                            for (MEE6Player player : players)
                                                            {
                                                                long id = Long.parseLong(player.getId());
                                                                long xp = player.getXp();
                                                                String name = player.getUsername();
                                                                String discriminator = player.getDiscriminator();
                                                                RankingSystem.setXP(connection, guildID, id, xp, name, discriminator);
                                                                transferredPlayers++;
                                                            }
                                                            page++;
                                                        }

                                                        if (transferredPlayers == 0)
                                                        {
                                                            EmbedBuilder builder1 = new EmbedBuilder()
                                                                    .setColor(Color.RED)
                                                                    .setDescription("Could not find any users!");

                                                            hook2.editOriginalEmbeds(builder1.build()).queue();
                                                            return;
                                                        }
                                                        EmbedBuilder builder1 = new EmbedBuilder()
                                                                .setColor(Color.green)
                                                                .setDescription("✅ XP has been successfully transferred for **" + transferredPlayers + "** users!");

                                                        hook2.editOriginalEmbeds(builder1.build()).queue();
                                                    }
                                                    catch (Exception ex)
                                                    {
                                                        if (ex instanceof FileNotFoundException)
                                                        {
                                                            EmbedBuilder embedBuilder1 = new EmbedBuilder()
                                                                    .setColor(Color.RED)
                                                                    .setDescription("Could not find any data for this guild! (Can you access the leaderboard?)");

                                                            hook2.editOriginalEmbeds(embedBuilder1.build()).queue();
                                                        }
                                                        else
                                                        {
                                                            EmbedBuilder embedBuilder1 = new EmbedBuilder()
                                                                    .setColor(Color.RED)
                                                                    .setDescription("MEE6 banned my IP. Please try again tomorrow.");

                                                            hook2.editOriginalEmbeds(embedBuilder1.build()).queue();
                                                        }
                                                    }
                                                });
                                    },
                                    30, TimeUnit.SECONDS,
                                    () ->
                                    {
                                        hook.editMessageEmbeds(new EmbedBuilder()
                                                        .setColor(Color.RED)
                                                        .setDescription("Timed out!")
                                                        .build())
                                                .setActionRow(Collections.emptyList()).queue();
                                    }
                            );
                        }
                );
    }
}
