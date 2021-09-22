package at.xirado.bean.command.slashcommands.moderation;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.moderation.CaseType;
import at.xirado.bean.moderation.ModCase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KickCommand extends SlashCommand
{
    public KickCommand()
    {
        setCommandData(new CommandData("kick", "kicks a member from this guild")
                .addOption(OptionType.USER, "user", "the user to kick", true)
                .addOption(OptionType.STRING, "reason", "the reason for this kick")
        );
        setRequiredBotPermissions(Permission.KICK_MEMBERS);
        setRequiredUserPermissions(Permission.KICK_MEMBERS);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Guild guild = event.getGuild();
        if (guild == null) return;
        Member member = event.getOption("user").getAsMember();
        String reason = event.getOption("reason") == null ? null : event.getOption("reason").getAsString();
        if (member == null)
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("This user is not member of this guild!")).setEphemeral(true).queue();
            return;
        }
        if (sender.getIdLong() == member.getIdLong())
        {
            ctx.reply(EmbedUtil.errorEmbed("You cannot kick yourself!")).setEphemeral(true).queue();
            return;
        }
        if (!sender.canInteract(member))
        {
            ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.kick.you_cannot_kick"))).setEphemeral(true).queue();
            return;
        }
        if (ctx.getGuildData().isModerator(member))
        {
            ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.kick.you_cannot_kick_moderator"))).setEphemeral(true).queue();
            return;
        }
        if (!guild.getSelfMember().canInteract(member))
        {
            ctx.reply(EmbedUtil.noEntryEmbed(ctx.getLocalized("commands.kick.i_cannot_kick"))).setEphemeral(true).queue();
            return;
        }
        MessageEmbed confirmationEmbed = new EmbedBuilder()
                .setColor(EmbedUtil.SUCCESS_COLOR)
                .setAuthor(ctx.getLocalized("commands.kick.has_been_kicked", member.getUser().getAsTag()), null, member.getUser().getEffectiveAvatarUrl())
                .addField(ctx.getLocalized("commands.reason"), reason == null ? ctx.getLocalized("commands.noreason") : reason, true)
                .build();
        EmbedBuilder dmEmbed = new EmbedBuilder()
                .setColor(CaseType.KICK.getEmbedColor())
                .setAuthor(ctx.getLocalized("commands.kick.you_have_been_kicked", guild.getName()), null, guild.getIconUrl())
                .addField("Moderator", sender.getAsMention()+" ("+sender.getUser().getAsTag()+")", true);
        if (reason != null)
            dmEmbed.addField(ctx.getLocalized("commands.reason"), reason, true);

        event.deferReply(true)
                .flatMap(hook -> member.getUser().openPrivateChannel())
                .flatMap(channel -> channel.sendMessageEmbeds(dmEmbed.build()))
                .flatMap(hook -> guild.kick(member, reason))
                .flatMap(x -> event.getHook().sendMessageEmbeds(confirmationEmbed))
                .queue(x -> {
                    ModCase.createModCase(CaseType.KICK, guild.getIdLong(), member.getIdLong(), sender.getIdLong(), reason);
                    if (ctx.getGuildData().getLogChannel() != null)
                    {
                        TextChannel logChannel = ctx.getGuildData().getLogChannel();
                        MessageEmbed logEmbed = new EmbedBuilder()
                                .setColor(CaseType.KICK.getEmbedColor())
                                .setAuthor("Kick • "+member.getUser().getAsTag(), null, member.getUser().getEffectiveAvatarUrl())
                                .addField(ctx.getLocalized("commands.reason"), reason, true)
                                .addField("Moderator", sender.getAsMention()+" ("+sender.getUser().getAsTag()+")", true)
                                .setFooter(ctx.getLocalized("commands.user_id", member.getIdLong()))
                                .build();
                        logChannel.sendMessageEmbeds(logEmbed).queue(s -> {}, e -> {});
                    }
                });
    }
}
