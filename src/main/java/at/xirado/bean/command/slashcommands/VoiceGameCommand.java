package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.CommandFlag;
import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VoiceGameCommand extends SlashCommand
{
    public static final Route ROUTE = Route.Invites.CREATE_INVITE;

    public VoiceGameCommand()
    {
        setCommandData(new CommandData("voicegame", "Create voice games")
                .addOptions(new OptionData(OptionType.STRING, "application", "the application to create")
                        .addChoice("Youtube Together", "755600276941176913")
                        .addChoice("Poker", "755827207812677713")
                        .addChoice("Betrayal.io", "773336526917861400")
                        .addChoice("Fishington.io", "814288819477020702")
                        .addChoice("Chess / CG 2 Dev", "832012586023256104")
                        .addChoice("Awkword", "879863881349087252")
                        .addChoice("Spellcast", "852509694341283871")
                        .addChoice("Doodlecrew", "878067389634314250")
                        .addChoice("Wordsnack", "879863976006127627")
                        .addChoice("Lettertile", "879863686565621790")
                        .setRequired(true)
                )
                .addOptions(new OptionData(OptionType.BOOLEAN, "hide", "Whether the response should be ephemeral (Only you can see this)")
                        .setRequired(false)
                )
        );
        setRequiredBotPermissions(Permission.CREATE_INSTANT_INVITE);
        addCommandFlags(CommandFlag.MUST_BE_IN_VC);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        long appId =  Long.parseUnsignedLong(event.getOption("application").getAsString());
        boolean ephemeral = event.getOption("hide") != null && event.getOption("hide").getAsBoolean();
        GuildVoiceState voiceState = event.getMember().getVoiceState();
        VoiceChannel voiceChannel = voiceState.getChannel();
        if (voiceChannel == null)
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("You must be in a VoiceChannel to do this!")).setEphemeral(true).queue();
            return;
        }
        event.deferReply(ephemeral)
                .flatMap(hook -> createInvite(3600, 0, voiceChannel, appId))
                .flatMap(url -> event.getHook().sendMessage(url))
                .queue();
    }

    /**
     * Creates a Discord VoiceChannel invite url with a custom target_application_id
     * @param maxAgeInSecs the max age of the invite in seconds (0 -> infinite)
     * @param maxUses how often this invite can be used (0 -> infinite)
     * @param voiceChannel The VoiceChannel the invite should be created on
     * @param applicationId The Application-ID to use
     * @return RestAction
     */
    public static RestAction<String> createInvite(int maxAgeInSecs, int maxUses,  VoiceChannel voiceChannel, long applicationId)
    {
        DataObject requestBody = DataObject.empty()
                .put("max_age", maxAgeInSecs)
                .put("max_uses", maxUses)
                .put("unique", true)
                .put("target_type", 2)
                .put("target_application_id", applicationId);

        return new RestActionImpl<>(
                voiceChannel.getJDA(),
                ROUTE.compile(voiceChannel.getId()),
                requestBody,
                (response, request) -> "https://discord.gg/" + response.getObject().getString("code"));
    }
}
