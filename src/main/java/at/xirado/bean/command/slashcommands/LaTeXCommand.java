package at.xirado.bean.command.slashcommands;

import at.xirado.bean.command.SlashCommand;
import at.xirado.bean.command.SlashCommandContext;
import at.xirado.bean.misc.EmbedUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LaTeXCommand extends SlashCommand
{
    public LaTeXCommand()
    {
        setCommandData(Commands.slash("latex", "Creates an image from a LaTeX Formula.")
                .addOption(OptionType.STRING, "formula", "Formula to create the image from.", true)
        );
    }


    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event, @Nullable Member sender, @NotNull SlashCommandContext ctx)
    {
        event.deferReply().queue();
        InteractionHook hook = event.getHook();
        String formula = event.getOption("formula").getAsString();
        TeXFormula tf;
        try
        {
            tf = new TeXFormula(formula);
        } catch (ParseException exception)
        {
            hook.sendMessageEmbeds(EmbedUtil.errorEmbed("An error occurred while parsing LaTeX formula!\n```\n" + exception.getMessage() + "\n```")).queue();
            return;
        }
        TeXIcon ti = tf.createTeXIcon(TeXConstants.STYLE_DISPLAY, 70);
        BufferedImage bimg = new BufferedImage(ti.getIconWidth(), ti.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = bimg.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, ti.getIconWidth(), ti.getIconHeight());
        JLabel jl = new JLabel();
        jl.setForeground(new Color(0, 0, 0));
        ti.paintIcon(jl, g2d, 0, 0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            ImageIO.write(bimg, "png", baos);
        } catch (IOException e)
        {
            hook.sendMessageEmbeds(EmbedUtil.errorEmbed("An error occurred!")).queue();
            return;
        }
        hook.sendMessage("").addFile(baos.toByteArray(), "card.png").queue();
    }
}
