package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.*;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Mock extends SlashCommand
{

    public Mock()
    {
        setCommandData(new CommandUpdateAction.CommandData("mock", "vAcCiNeS cAuSe AuTiSm")
            .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "text", "the text to mock")
                .setRequired(true)
            )
        );
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
    {
        String toMock = event.getOption("text").getAsString();
        StringBuilder sensitive = new StringBuilder();
        for(int i = 0; i < toMock.length(); i++)
        {
            if(i%2 == 0)
            {
                sensitive.append(String.valueOf(toMock.charAt(i)).toLowerCase());
            }
            else
            {
                sensitive.append(String.valueOf(toMock.charAt(i)).toUpperCase());
            }
        }
        ctx.reply("<:mock:773566020588666961> "+sensitive.toString()+" <:mock:773566020588666961>").queue();

    }
}
