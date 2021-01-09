package at.Xirado.Bean.Handlers;

import at.Xirado.Bean.Misc.SQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class BlacklistManager
{
    public ConcurrentHashMap<Long, ArrayList<String>> blacklistedWords;

    public BlacklistManager()
    {
        this.blacklistedWords = new ConcurrentHashMap<>();
    }

    public boolean containsBlacklistedWord(long guildID, String word)
    {
        String qry = "SELECT 1 FROM blacklistedWords WHERE guildID = ? AND word = ?";
        try
        {
            PreparedStatement ps = SQL.con.prepareStatement(qry);
            ps.setLong(1, guildID);
            ps.setString(2, word.toUpperCase());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return false;
        }
    }

    public void removeBlacklistedWord(long guildID, String word)
    {
        if(!containsBlacklistedWord(guildID, word.toUpperCase()))
        {
            return;
        }
        ArrayList<String> bannedwords = getBlacklistedWords(guildID);
        bannedwords.remove(word.toUpperCase());
        blacklistedWords.put(guildID, bannedwords);
        String qry = "DELETE FROM blacklistedWords WHERE guildID = ? AND word = ?";
        try
        {
            PreparedStatement ps = SQL.con.prepareStatement(qry);
            ps.setLong(1, guildID);
            ps.setString(2, word.toUpperCase());
            ps.execute();

        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    public void addBlacklistedWord(long guildID, String word)
    {
        if(containsBlacklistedWord(guildID, word.toUpperCase()))
        {
            return;
        }
        ArrayList<String> bannedwords = getBlacklistedWords(guildID);
        bannedwords.add(word.toUpperCase());
        blacklistedWords.put(guildID, bannedwords);
        String qry = "INSERT INTO blacklistedWords (guildID, word) values (?,?)";
        try
        {
            PreparedStatement ps = SQL.con.prepareStatement(qry);
            ps.setLong(1, guildID);
            ps.setString(2, word.toUpperCase());
            ps.execute();

        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    public ArrayList<String> getBlacklistedWords(long guildID)
    {
        if(this.blacklistedWords.containsKey(guildID))
        {
            return this.blacklistedWords.get(guildID);
        }
        String qry = "SELECT word FROM blacklistedWords WHERE guildID = ?";
        try
        {
            PreparedStatement ps = SQL.con.prepareStatement(qry);
            ps.setLong(1,  guildID);
            ResultSet rs = ps.executeQuery();
            ArrayList<String> blacklistedWords = new ArrayList<>();
            while(rs.next())
            {
                blacklistedWords.add(rs.getString("word"));
            }
            return blacklistedWords;
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return new ArrayList<>();
        }
    }
}
