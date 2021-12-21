package at.xirado.bean.command.slashcommands.music;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.music.GuildAudioPlayer;
import lavalink.client.io.filters.Filters;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VolumeCommand extends SlashCommand
{
    public VolumeCommand()
    {
        setCommandData(new CommandData("volume", "Changes the volume of the player.")
                .addOption(OptionType.INTEGER, "volume", "Volume in percent.", true)
        );
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.DJ_ONLY);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        Member member = event.getMember();
        OptionMapping option = event.getOption("volume");
        GuildData guildData = ctx.getGuildData();

        // check if earrape is allowed (can be changed in the dashboard)
        boolean allowEarRape = !guildData.isNull("allow_earrape") && guildData.getBoolean("allow_earrape");
        int volume = (int) Math.max(1, Math.min(allowEarRape ? 300 : 100, option.getAsLong()));

        GuildAudioPlayer guildAudioPlayer = Bean.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        guildAudioPlayer.getPlayer().getFilters().setVolume((float) volume / 100).commit();
        ctx.sendSimpleEmbed("The volume has been adjusted to `" + volume + "%`!");
    }

}
