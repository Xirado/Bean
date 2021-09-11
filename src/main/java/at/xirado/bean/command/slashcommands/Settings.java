package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Settings extends SlashCommand
{
    public Settings()
    {
        setCommandData(new CommandData("settings", "change some settings"));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Set<TextChannel> channels = event.getGuild().getTextChannels().stream()
                .filter(channel -> channel.canTalk(event.getGuild().getSelfMember()))
                .collect(Collectors.toUnmodifiableSet());
        Set<SelectOption> options = new HashSet<>();
        for (TextChannel channel : channels)
        {
            options.add(SelectOption.of("#"+channel.getName(), "channel:"+channel.getIdLong()));
        }
        SelectionMenu menu = SelectionMenu.create("channel")
                .setPlaceholder("Wähle einen Kanal aus")
                .addOptions(options)
                .build();event.replyEmbeds(EmbedUtil.defaultEmbed("Bitte wähle einen Kanal aus, der zum Protokollieren verwendet werden soll!"))
                .setEphemeral(true)
                .addActionRows(ActionRow.of(menu))
                .queue();
    }
}
