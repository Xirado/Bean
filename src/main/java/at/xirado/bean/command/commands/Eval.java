package at.xirado.bean.command.commands;

import at.xirado.bean.command.Command;
import at.xirado.bean.command.CommandContext;
import at.xirado.bean.command.CommandFlag;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

public class Eval extends Command
{
    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("groovy");
    public static final Collection<String> DEFAULT_IMPORTS =
            Arrays.asList(
                    "net.dv8tion.jda.api.entities.impl",
                    "net.dv8tion.jda.api.managers",
                    "net.dv8tion.jda.api.entities",
                    "net.dv8tion.jda.api",
                    "net.dv8tion.jda.api.utils",
                    "net.dv8tion.jda.api.utils.data",
                    "net.dv8tion.jda.internal.requests",
                    "net.dv8tion.jda.api.requests",
                    "java.lang",
                    "java.io",
                    "java.math",
                    "java.util",
                    "java.util.concurrent",
                    "java.time"
            );
    public static final Collection<String> DEFAULT_STATIC_IMPORTS =
            Arrays.asList("at.xirado.bean.misc.EvalUtil");


    public Eval()
    {
        super("eval", "evaluates some code", "eval [code]");
        setCommandFlags(CommandFlag.DEVELOPER_ONLY);
    }

    @Override
    public void executeCommand(GuildMessageReceivedEvent event, CommandContext context)
    {
        SCRIPT_ENGINE.put("guild", event.getGuild());
        SCRIPT_ENGINE.put("author", event.getAuthor());
        SCRIPT_ENGINE.put("member", event.getMember());
        SCRIPT_ENGINE.put("channel", event.getChannel());
        SCRIPT_ENGINE.put("jda", event.getJDA());
        SCRIPT_ENGINE.put("api", event.getJDA());
        SCRIPT_ENGINE.put("bot", event.getJDA().getSelfUser());
        SCRIPT_ENGINE.put("selfuser", event.getJDA().getSelfUser());
        SCRIPT_ENGINE.put("selfmember", event.getGuild().getSelfMember());
        var toEval = new StringBuilder();
        String evalString = context.getArguments().toString();
        if (evalString.startsWith("```") && evalString.endsWith("```"))
        {
            int index = evalString.indexOf(" ");
            evalString = evalString.substring(index, evalString.length()-3);
        }
        DEFAULT_IMPORTS.forEach(imp -> toEval.append("import ").append(imp).append(".*;\n"));
        DEFAULT_STATIC_IMPORTS.forEach(imp -> toEval.append("import static ").append(imp).append(".*;\n"));
        toEval.append(evalString);
        try {
            var evaluated = SCRIPT_ENGINE.eval(toEval.toString());
            if (evaluated instanceof RestAction<?> action)
            {
                action.queue(s -> event.getMessage().reply("RestAction executed without errors! Received object of type `"+s.getClass().getSimpleName()+"`").mentionRepliedUser(false).allowedMentions(EnumSet.of(Message.MentionType.USER, Message.MentionType.EMOTE)).queue(), e -> event.getMessage().reply("RestAction returned failure!\n```fix\n"+ ExceptionUtils.getStackTrace(e)+"\n```").allowedMentions(EnumSet.of(Message.MentionType.USER, Message.MentionType.EMOTE)).mentionRepliedUser(false).queue());
                return;
            }
            if (evaluated == null)
            {
                event.getMessage().addReaction("✅").queue();
                return;
            }
            if (evaluated instanceof CharSequence || evaluated instanceof Number)
            {
                event.getMessage().reply("Return value: `"+evaluated +"`").mentionRepliedUser(false).allowedMentions(EnumSet.of(Message.MentionType.USER, Message.MentionType.EMOTE)).queue();
                return;
            }
            event.getMessage().reply("Got return value of type `"+evaluated.getClass().getSimpleName()+"`").mentionRepliedUser(false).allowedMentions(EnumSet.of(Message.MentionType.USER, Message.MentionType.EMOTE)).queue();

        }
        catch (ScriptException ex) {
            event.getMessage().reply(ex.getMessage()).mentionRepliedUser(false).allowedMentions(EnumSet.of(Message.MentionType.USER, Message.MentionType.EMOTE)).queue();
        }
    }
}
