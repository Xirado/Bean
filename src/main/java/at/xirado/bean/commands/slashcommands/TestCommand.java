package at.xirado.bean.commands.slashcommands;

import at.xirado.bean.commandmanager.CommandContext;
import at.xirado.bean.commandmanager.SlashCommand;
import at.xirado.bean.misc.JSON;
import at.xirado.bean.translation.FormattedDuration;
import at.xirado.bean.translation.I18n;
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.text.Format;
import java.util.Arrays;
import java.util.Map;

public class TestCommand extends SlashCommand
{
    public TestCommand()
    {
        setCommandData(new CommandUpdateAction.CommandData("test", "this command is only for test purposes")
            .addOption(new CommandUpdateAction.OptionData(Command.OptionType.STRING, "this", "is a test").addChoice("that", "1").setRequired(true))
        );
        Global(false);
        setEnabledGuilds(Arrays.asList(815597207617142814L));
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event, @Nullable Member sender, @NotNull CommandContext ctx)
    {
        try
        {
            String requestURL = "https://v2.jokeapi.dev/joke/Miscellaneous,Dark,Pun";
            URL url = new URL(requestURL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            JSON json = JSON.parse(conn.getInputStream());
            if(json == null)
            {
                ctx.replyError(ctx.getLocalized("commands.fact.api_down")).queue();
                return;
            }
            Map<String, Boolean> flags = (Map<String, Boolean>) json.getObject("flags");
            for(Map.Entry<String, Boolean> entry : flags.entrySet())
            {
                String flag = entry.getKey();
                Boolean value = entry.getValue();
                System.out.println(flag+" "+value);
            }
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
