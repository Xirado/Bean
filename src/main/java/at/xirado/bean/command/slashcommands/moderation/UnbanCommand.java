package at.xirado.bean.command.slashcommands.moderation;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnbanCommand extends SlashCommand
{
    public UnbanCommand()
    {
        setCommandData(new CommandData("unban", "unbans a user from this guild")
                .addOption(OptionType.USER, "user", "the user to unban", true)
        );
        setRequiredBotPermissions(Permission.BAN_MEMBERS);
        setRequiredUserPermissions(Permission.BAN_MEMBERS);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild guild = event.getGuild();
        if (guild == null) return;
        User user = event.getOption("user").getAsUser();
        event.deferReply(true)
                .flatMap(ban -> guild.unban(user))
                .flatMap(ban -> event.getHook().sendMessageEmbeds(EmbedUtil.successEmbed(user.getAsTag()+" has been unbanned!")))
                .queue(null,
                        new ErrorHandler().handle(ErrorResponse.UNKNOWN_BAN, (e) -> event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("This user is not banned!")).queue()));
    }
}
