package at.xirado.bean.handlers;

import at.xirado.bean.misc.Database;
import at.xirado.bean.misc.Util;

import java.sql.Connection;
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
        Connection connection = Database.getConnectionFromPool();
        if(connection == null) return false;
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.setString(2, word.toUpperCase());
            ResultSet rs = ps.executeQuery();
            boolean x = rs.next();
            return x;
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return false;
        } finally
        {
            Util.closeQuietly(connection);
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
        Connection connection = Database.getConnectionFromPool();
        if(connection == null) return;
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.setString(2, word.toUpperCase());
            ps.execute();

        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        } finally
        {
            Util.closeQuietly(connection);
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
        Connection connection = Database.getConnectionFromPool();
        if(connection == null) return;
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.setString(2, word.toUpperCase());
            ps.execute();

        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public ArrayList<String> getBlacklistedWords(long guildID)
    {
        if(this.blacklistedWords.containsKey(guildID))
        {
            return this.blacklistedWords.get(guildID);
        }
        String qry = "SELECT word FROM blacklistedWords WHERE guildID = ?";
        Connection connection = Database.getConnectionFromPool();
        if(connection == null) return null;
        try(PreparedStatement ps = connection.prepareStatement(qry))
        {
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
        } finally
        {
            Util.closeQuietly(connection);
        }
    }
}
