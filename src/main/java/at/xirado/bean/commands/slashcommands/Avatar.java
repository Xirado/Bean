package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandutil.SlashCommandContext;
import at.xirado.bean.objects.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;

public class Avatar extends SlashCommand
{

	public Avatar()
	{
		setCommandData(new CommandUpdateAction.CommandData("avatar", "Gets the avatar of a user")
				.addOption(new CommandUpdateAction.OptionData(Command.OptionType.USER, "user", "the user you want to get the avatar from"))
		);
	}

	@Override
	public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
	{
		User user;
		SlashCommandEvent.OptionData option = event.getOption("user");
		if(option != null) user = option.getAsUser();
		else user = event.getUser();
		EmbedBuilder b = new EmbedBuilder()
				.setImage(user.getEffectiveAvatarUrl()+"?size=512")
				.setColor(Color.magenta)
				.setTimestamp(Instant.now())
				.setAuthor(ctx.getLocalized("commands.avatar_title", user.getAsTag()), null, user.getEffectiveAvatarUrl());

		ctx.reply(b.build()).queue();
	}
}
