package at.xirado.bean.event;

import at.xirado.bean.Bean;
import at.xirado.bean.command.CommandContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SlashCommandListener extends ListenerAdapter {
    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
        if (!event.isFromGuild())
            return;

        if (Bean.getInstance().isDebug() && Bean.WHITELISTED_USERS.stream().noneMatch(x -> x == event.getUser().getIdLong())) {
            event.reply(CommandContext.ERROR_EMOTE + " Bot is in debug mode! Only whitelisted users can execute commands!").setEphemeral(true).queue();
            return;
        }

        Bean.getInstance().getInteractionHandler().handleCommand(event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.isFromGuild())
            return;

        Bean.getInstance().getInteractionHandler().handleAutocomplete(event);
    }
}
