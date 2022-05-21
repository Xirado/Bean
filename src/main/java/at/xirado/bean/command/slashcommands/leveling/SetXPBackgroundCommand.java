package at.xirado.bean.command.slashcommands.leveling;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.data.RankingSystem;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.AttachmentProxy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SetXPBackgroundCommand extends SlashCommand {
    public SetXPBackgroundCommand() {
        setCommandData(Commands.slash("setxpcard", "Updates /rank background.")
                .addOptions(new OptionData(OptionType.ATTACHMENT, "background", "The image to choose (1200x300 is ideal, 15MB max)")
                        .setRequired(true)
                )
                .addOptions(new OptionData(OptionType.INTEGER, "color", "primary color")
                        .setRequired(true)
                        .addChoice("Red", 0xD0312D)
                        .addChoice("Green", 0x32CD32)
                        .addChoice("Blue", 0x0C71E0)
                        .addChoice("Purple", 0x842BD7)
                        .addChoice("Pink", 0xf542ec)
                        .addChoice("Mint", 0x42f58d)
                        .addChoice("Orange", 0xd48e15)
                )
        );
    }

    public static final List<String> SUPPORTED_EXTENSIONS = List.of("png", "jpg", "jpeg", "gif");

    public static final File DIRECTORY = new File("backgrounds");
    public static final int MAX_SIZE = 1000 * 1000 * 15; // 15 MB

    static {
        if (!DIRECTORY.exists())
            DIRECTORY.mkdir();
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        try {
            Message.Attachment attachment = event.getOption("background").getAsAttachment();
            String extension = attachment.getFileExtension();
            if (extension == null || (!SUPPORTED_EXTENSIONS.contains(extension))) {
                event.replyEmbeds(EmbedUtil.errorEmbed("Unsupported type! Supported extensions are: `" + String.join(", ", SUPPORTED_EXTENSIONS) + "`")).setEphemeral(true).queue();
                return;
            }

            if (attachment.getSize() > MAX_SIZE) {
                event.replyEmbeds(EmbedUtil.errorEmbed("The image you uploaded is larger than **15 MB**!")).setEphemeral(true).queue();
                return;
            }

            event.deferReply(true).queue();
            AttachmentProxy proxy = attachment.getProxy();
            String fullName = event.getInteraction().getId() + "." + extension;

            File file = new File(DIRECTORY, fullName);
            proxy.downloadToFile(file, 1200, 300).get();
            RankingSystem.setPreferredCard(event.getUser(), fullName, event.getOption("color").getAsInt());
            event.getHook().sendMessageEmbeds(EmbedUtil.successEmbed("Your background has been updated!")).queue();
        } catch (InterruptedException | ExecutionException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
