package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;

public class AvatarCommand extends SlashCommand
{
    public AvatarCommand()
    {
        setCommandData(Commands.slash("avatar", "Gets the avatar of a user.")
                .addOption(OptionType.USER, "user", "User to get the avatar from.", false)
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        OptionMapping option = event.getOption("user");
        User user = option != null ? option.getAsUser() : event.getUser();
        EmbedBuilder b = new EmbedBuilder()
                .setImage(user.getEffectiveAvatarUrl() + "?size=512")
                .setColor(Color.magenta)
                .setTimestamp(Instant.now())
                .setAuthor(ctx.getLocalized("commands.avatar_title", user.getAsTag()), null, user.getEffectiveAvatarUrl());

        ctx.reply(b.build()).queue();
    }
}
