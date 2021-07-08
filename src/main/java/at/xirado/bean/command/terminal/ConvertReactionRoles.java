package at.xirado.bean.command.terminal;

import at.xirado.bean.Bean;
import at.xirado.bean.command.ConsoleCommand;
import at.xirado.bean.data.GuildData;
import at.xirado.bean.data.GuildManager;
import at.xirado.bean.data.ReactionRole;
import at.xirado.bean.data.database.Database;
import at.xirado.bean.log.MCColor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.sql.Connection;
import java.sql.ResultSet;

public class ConvertReactionRoles extends ConsoleCommand
{
    public ConvertReactionRoles()
    {
        super.invoke = "convertreactionroles";
        super.description = "Converts old reaction role scheme to GuildData implementation";
    }

    @Override
    public void executeCommand(String invoke, String[] args)
    {
        String qry = "SELECT * FROM reactionRoles";
        Connection connection = Database.getConnectionFromPool();
        if (connection == null)
        {
            System.out.println(MCColor.translate("&eCould not get connection! Aborting"));
            return;
        }
        int entries = 0;
        int done = 0;
        try (var ps = connection.prepareStatement(qry))
        {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                entries++;
                long messageId = rs.getLong("messageID");
                String emote = rs.getString("emoticon");
                long roleId = rs.getLong("roleID");
                Role role = Bean.getInstance().getJDA().getRoleById(roleId);
                if (role == null) continue;
                Guild guild = role.getGuild();
                GuildData guildData = GuildManager.getGuildData(guild);
                ReactionRole reactionRole = new ReactionRole();
                reactionRole.setMessageId(messageId);
                reactionRole.setRoleId(roleId);
                reactionRole.setEmote(emote);
                guildData.addReactionRoles(reactionRole).update();
                done++;
            }
            System.out.println("Found " + entries + " entries in the database!");
            System.out.println("Successfully updated " + done + " values!");
        } catch (Exception ex)
        {
            System.out.println(MCColor.translate("&cAn error occured!"));
            ex.printStackTrace();
        }
    }
}
