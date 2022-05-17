package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

public class SlapCommand extends SlashCommand {
    private static final int RAW_AVATAR_SIZE = 512;
    private static final int RAW_AVATAR_BORDER_SIZE = RAW_AVATAR_SIZE / 64;
    private static final int WIDTH = 960;
    private static final int HEIGHT = 500;

    public SlapCommand() {
        setCommandData(Commands.slash("slap", "Slap someone")
                .addOption(OptionType.USER, "user", "User to slap.", true)
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @NotNull SlashCommandContext ctx) {
        User author = event.getUser();
        User target = event.getOption("user").getAsUser();
        if (target.getIdLong() == event.getUser().getIdLong()) {
            event.replyEmbeds(EmbedUtil.defaultEmbed("Why do you want to slap yourself :(")).queue();
            return;
        }
        boolean reversed = (1 + ThreadLocalRandom.current().nextInt(10)) == 1;
        User slapper = reversed ? target : author;
        User victim = reversed ? author : target;
        event.deferReply().queue();
        try {
            byte[] image = generateImage(slapper, victim);
            WebhookMessageAction<Message> action = event.getHook().sendFile(image, "slap.png");
            if (reversed)
                action.setContent("Lol, better luck next time.");
            action.queue();
        } catch (IOException ex) {
            LoggerFactory.getLogger(SlapCommand.class).error("Could not generate image!", ex);
            event.getHook().sendMessageEmbeds(EmbedUtil.errorEmbed("An error occurred!")).queue();
        }
    }

    public static BufferedImage getAvatar(User user, int size) throws IOException {
        var avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl() + "?size=" + RAW_AVATAR_SIZE));
        var roundAvatar = new BufferedImage(RAW_AVATAR_SIZE, RAW_AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
        var avatarGraphics = roundAvatar.createGraphics();
        avatarGraphics.setColor(Color.white);
        avatarGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        avatarGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        avatarGraphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        avatarGraphics.fillArc(0, 0, RAW_AVATAR_SIZE, RAW_AVATAR_SIZE, 0, 360);
        var rawAvatarSize = RAW_AVATAR_SIZE - RAW_AVATAR_BORDER_SIZE * 2;
        avatarGraphics.setClip(new Ellipse2D.Float(RAW_AVATAR_BORDER_SIZE, RAW_AVATAR_BORDER_SIZE, rawAvatarSize, rawAvatarSize));
        avatarGraphics.drawImage(avatar, RAW_AVATAR_BORDER_SIZE, RAW_AVATAR_BORDER_SIZE, rawAvatarSize, rawAvatarSize, null);
        avatarGraphics.dispose();
        var downscaledAvatar = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        var avatarDownscaledAvatar = downscaledAvatar.createGraphics();
        avatarDownscaledAvatar.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        avatarDownscaledAvatar.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        avatarDownscaledAvatar.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        avatarDownscaledAvatar.drawImage(roundAvatar, 0, 0, size, size, null);
        avatarDownscaledAvatar.dispose();
        return downscaledAvatar;
    }

    public static byte[] generateImage(User slapper, User victim) throws IOException {
        var background = SlapCommand.class.getResourceAsStream("/assets/misc/slap.jpg");
        var userAvatar = getAvatar(slapper, 200);
        var targetAvatar = getAvatar(victim, 250);
        var bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        var graphics = bufferedImage.createGraphics();
        var backgroundBufImg = ImageIO.read(background);
        graphics.drawImage(backgroundBufImg, 0, 0, WIDTH, HEIGHT, null);
        graphics.drawImage(userAvatar, 530, 70, 200, 200, null);
        graphics.drawImage(targetAvatar, 200, 180, 250, 250, null);
        var byteOutputStream = new ByteArrayOutputStream();
        graphics.dispose();
        try (byteOutputStream) {
            ImageIO.write(bufferedImage, "png", byteOutputStream);
            return byteOutputStream.toByteArray();
        }
    }
}
