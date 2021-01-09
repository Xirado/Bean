package at.Xirado.Bean.Commands;

import at.Xirado.Bean.CommandManager.Command;
import at.Xirado.Bean.CommandManager.CommandEvent;
import at.Xirado.Bean.CommandManager.CommandType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class Warns extends Command
{
    public Warns(JDA jda)
    {
        super(jda);
        this.invoke = "warns";
        this.commandType = CommandType.MODERATION;
        this.description = "Lists all the warns of an user";
        this.neededPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.aliases = new String[]{"infractions"};
        this.usage = "warns [@User/ID]";
    }

    @Override
    public void execute(CommandEvent event)
    {
        String[] args = event.getArguments().getArguments();
        Guild guild = event.getGuild();
        Member member = event.getMember();


    }
}
