package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandContext;
import net.dv8tion.jda.api.events.interaction.ApplicationCommandAutocompleteEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.getGuild() == null)
            return;
        if (GuildJoinListener.isGuildBanned(event.getGuild().getIdLong()))
            return;
        if (Bean.getInstance().isDebug() && Bean.WHITELISTED_USERS.stream().noneMatch(x -> x == event.getUser().getIdLong())) {
            event.reply(CommandContext.ERROR_EMOTE + " Bot is in debug mode! Only whitelisted users can execute commands!").setEphemeral(true).queue();
            return;
        }
        Bean.getInstance().getSlashCommandHandler().handleSlashCommand(event, event.getMember());
    }

    @Override
    public void onApplicationCommandAutocomplete(@NotNull ApplicationCommandAutocompleteEvent event) {
        if (event.getGuild() == null)
            return;
        if (GuildJoinListener.isGuildBanned(event.getGuild().getIdLong()))
            return;
        Bean.getInstance().getSlashCommandHandler().handleAutocomplete(event);
    }
}
