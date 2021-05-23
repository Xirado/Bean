package at.xirado.bean.data;

import at.xirado.bean.misc.Database;
import at.xirado.bean.misc.JSON;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RankingSystem
{

    private static final Logger LOGGER = LoggerFactory.getLogger(RankingSystem.class);
    private static JSON colorData = null;
    private static Font FONT;

    private static final int CARD_WIDTH = 1200;
    private static final int CARD_HEIGHT = CARD_WIDTH / 4;
    private static final int CARD_RATIO = CARD_WIDTH / CARD_HEIGHT;

    private static final float FONT_SIZE = 60f;
    private static final float DISCRIMINATOR_FONT_SIZE = (float) (FONT_SIZE / 1.5);

    private static final int RAW_AVATAR_SIZE = 512;
    private static final int RAW_AVATAR_BORDER_SIZE = RAW_AVATAR_SIZE / 64;

    private static final int BORDER_SIZE = CARD_HEIGHT / 13;

    private static final int AVATAR_SIZE = CARD_HEIGHT - BORDER_SIZE * 2;

    private static final int XP_BAR_WIDTH = CARD_WIDTH - AVATAR_SIZE - BORDER_SIZE * 3;
    private static final int XP_BAR_HEIGHT = CARD_HEIGHT / 5;

    static{
        try{
            FONT = Font.createFont(Font.TRUETYPE_FONT, RankingSystem.class.getResourceAsStream("/assets/fonts/NotoSans.ttf"));
            colorData = JSON.parse(RankingSystem.class.getResourceAsStream("/assets/wildcards/ColorInfo.json"));

        }
        catch(FontFormatException | IOException e){
            LOGGER.error("Couldn't load font from resources", e);
        }
    }

    /**
     * returns the relative XP needed to level up to the next level
     * @param currentLevel the current level
     * @return relative XP needed to level up
     */
    public static long getXPToLevelUp(int currentLevel)
    {
        double x = 5*(currentLevel*currentLevel) + (50 * currentLevel) + 100;
        return (long) x;
    }

    /**
     * returns the current level
     * @param totalXP total xp
     * @return the level
     */
    public static int getLevel(long totalXP)
    {
        if(totalXP < 100) return 0;
        int counter = 0;
        long total = 0L;
        while(true)
        {
            long neededForNextLevel = getXPToLevelUp(counter);
            if(neededForNextLevel > totalXP) return counter;
            total += neededForNextLevel;
            if(total > totalXP) return counter;
            counter++;
        }

    }

    /**
     * returns the total xp needed to reach a certain level
     * @param level the level
     * @return total xp needed to reach that level
     */
    public static long getTotalXPNeeded(int level)
    {
        long x = 0;
        for(int i = 0; i < level; i++)
        {
            x += getXPToLevelUp(i);
        }
        return x;
    }

    public static long getTotalXP(@Nonnull Connection connection, long guildID, long userID)
    {
        try(var ps = connection.prepareStatement("SELECT * FROM levels WHERE guildID = ? AND userID = ?"))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                return rs.getLong("totalXP");
            }
            return 0L;
        }catch (SQLException ex)
        {
            LOGGER.error("Could not get total xp! (guild "+guildID+", user "+userID+")", ex);
            return -1L;
        }
    }

    public static long getTotalXP(long guildID, long userID)
    {
        Connection connection = Database.getConnectionFromPool();
        if(connection == null) return -1L;
        try(var ps = connection.prepareStatement("SELECT * FROM levels WHERE guildID = ? AND userID = ?"))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                return rs.getLong("totalXP");
            }
            return 0L;
        }catch (SQLException ex)
        {
            LOGGER.error("Could not get total xp! (guild "+guildID+", user "+userID+")", ex);
            return -1L;
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static void addXP(long guildID, long userID, long addedAmount)
    {
        Connection connection = Database.getConnectionFromPool();
        if(connection == null){
            LOGGER.error("Could not get connection from db pool!", new SQLException("Connection == null!"));
            return;
        }
        if(hasEntry(guildID, userID))
        {
            try(var ps = connection.prepareStatement("UPDATE levels SET totalXP = ? WHERE guildID = ? AND userID = ?"))
            {
                ps.setLong(1, getTotalXP(connection, guildID, userID)+addedAmount);
                ps.setLong(2, guildID);
                ps.setLong(3, userID);
                ps.execute();

            }catch (SQLException ex)
            {
                ex.printStackTrace();
            } finally
            {
                Util.closeQuietly(connection);
            }
        }else {
            try(var ps = connection.prepareStatement("INSERT INTO levels (guildID, userID, totalXP) values (?,?,?)"))
            {
                ps.setLong(1, guildID);
                ps.setLong(2, userID);
                ps.setLong(3, addedAmount);
                ps.execute();

            }catch (SQLException ex)
            {
                ex.printStackTrace();
            } finally
            {
                Util.closeQuietly(connection);
            }
        }
    }

    public static void addXP(@Nonnull Connection connection, long guildID, long userID, long addedAmount)
    {
        if(hasEntry(guildID, userID))
        {
            try(var ps = connection.prepareStatement("UPDATE levels SET totalXP = ? WHERE guildID = ? AND userID = ?"))
            {
                ps.setLong(1, getTotalXP(connection, guildID, userID)+addedAmount);
                ps.setLong(2, guildID);
                ps.setLong(3, userID);
                ps.execute();

            }catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }else {
            try(var ps = connection.prepareStatement("INSERT INTO levels (guildID, userID, totalXP) values (?,?,?)"))
            {
                ps.setLong(1, guildID);
                ps.setLong(2, userID);
                ps.setLong(3, addedAmount);
                ps.execute();

            }catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public static void setXP(long guildID, long userID, long setAmount)
    {
        Connection connection = Database.getConnectionFromPool();
        if(connection == null){
            LOGGER.error("Could not get connection from db pool!", new SQLException("Connection == null!"));
            return;
        }
        if(hasEntry(guildID, userID))
        {
            try(var ps = connection.prepareStatement("UPDATE levels SET totalXP = ? WHERE guildID = ? AND userID = ?"))
            {
                ps.setLong(1, setAmount);
                ps.setLong(2, guildID);
                ps.setLong(3, userID);
                ps.execute();

            }catch (SQLException ex)
            {
                ex.printStackTrace();
            } finally
            {
                Util.closeQuietly(connection);
            }
        }else {
            try(var ps = connection.prepareStatement("INSERT INTO levels (guildID, userID, totalXP) values (?,?,?)"))
            {
                ps.setLong(1, guildID);
                ps.setLong(2, userID);
                ps.setLong(3, setAmount);
                ps.execute();

            }catch (SQLException ex)
            {
                ex.printStackTrace();
            } finally
            {
                Util.closeQuietly(connection);
            }
        }
    }

    public static void setXP(@Nonnull Connection connection, long guildID, long userID, long setAmount) throws SQLException
    {
        String sql = "INSERT INTO levels (guildID, userID, totalXP) VALUES (?,?,?) ON DUPLICATE KEY UPDATE totalXP = ?";
        var ps = connection.prepareStatement(sql);
        ps.setLong(1, guildID);
        ps.setLong(2, userID);
        ps.setLong(3, setAmount);
        ps.setLong(4, setAmount);
        ps.execute();
        ps.close();
    }

    public static boolean hasEntry(long guildID, long userID)
    {
        Connection connection = Database.getConnectionFromPool();
        if(connection == null) return false;
        try(var ps = connection.prepareStatement("SELECT * FROM levels WHERE guildID = ? AND userID = ?"))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }catch (SQLException ex)
        {
            LOGGER.error("Could not check if user has entry! (guild "+guildID+", user "+userID+")", ex);
            return false;
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static boolean hasEntry(@Nonnull Connection connection, long guildID, long userID)
    {
        try(var ps = connection.prepareStatement("SELECT * FROM levels WHERE guildID = ? AND userID = ?"))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }catch (SQLException ex)
        {
            LOGGER.error("Could not check if user has entry! (guild "+guildID+", user "+userID+")", ex);
            return false;
        }
    }

    public static String getPreferredCard(@Nonnull User user)
    {
        Connection connection = Database.getConnectionFromPool();
        if(connection == null) return "card1";
        try(var ps = connection.prepareStatement("SELECT * FROM wildcardSettings WHERE userID = ?"))
        {
            ps.setLong(1, user.getIdLong());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("card") : "card1";
        }catch (SQLException ex)
        {
            LOGGER.error("Could not get user preferred wildcard background (user "+user.getIdLong()+")", ex);
            return "card1";
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static void setPreferredCard(@Nonnull User user, @Nonnull String card)
    {
        Connection connection = Database.getConnectionFromPool();
        if(connection == null) return;
        try(var ps = connection.prepareStatement("INSERT INTO wildcardSettings (userID, card) VALUES (?,?) ON DUPLICATE KEY UPDATE card = ?"))
        {
            ps.setLong(1, user.getIdLong());
            ps.setString(2, card);
            ps.execute();
        }catch (SQLException ex)
        {
            LOGGER.error("Could not get user preferred wildcard background (user "+user.getIdLong()+")", ex);
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static String getPreferredCard(@Nonnull Connection connection, @Nonnull User user)
    {
        try(var ps = connection.prepareStatement("SELECT * FROM wildcardSettings WHERE userID = ?"))
        {
            ps.setLong(1, user.getIdLong());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("card") : "card1";
        }catch (SQLException ex)
        {
            LOGGER.error("Could not get user preferred wildcard background (user "+user.getIdLong()+")", ex);
            return "card1";
        }
    }

    public static void setPreferredCard(@Nonnull Connection connection, @Nonnull User user, @Nonnull String card)
    {
        try(var ps = connection.prepareStatement("INSERT INTO wildcardSettings (userID, card) VALUES (?,?) ON DUPLICATE KEY UPDATE card = ?"))
        {
            ps.setLong(1, user.getIdLong());
            ps.setString(2, card);
            ps.execute();
        }catch (SQLException ex)
        {
            LOGGER.error("Could not get user preferred wildcard background (user "+user.getIdLong()+")", ex);
        }
    }

    public static byte[] generateLevelCard(@Nonnull User user, @Nonnull Guild guild){
        try{
            var avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl() + "?size=" + RAW_AVATAR_SIZE));

            // make the avatar round
            var roundAvatar = new BufferedImage(RAW_AVATAR_SIZE, RAW_AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
            var roundAvatarG= roundAvatar.createGraphics();
            roundAvatarG.setColor(Color.white);
            roundAvatarG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            roundAvatarG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            roundAvatarG.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            roundAvatarG.fillArc(0, 0, RAW_AVATAR_SIZE, RAW_AVATAR_SIZE, 0, 360);
            var rawAvatarSize = RAW_AVATAR_SIZE - RAW_AVATAR_BORDER_SIZE * 2;
            roundAvatarG.setClip(new Ellipse2D.Float(RAW_AVATAR_BORDER_SIZE, RAW_AVATAR_BORDER_SIZE, rawAvatarSize, rawAvatarSize));
            roundAvatarG.drawImage(avatar, RAW_AVATAR_BORDER_SIZE, RAW_AVATAR_BORDER_SIZE, rawAvatarSize, rawAvatarSize, null);
            roundAvatarG.dispose();

            // downscale the avatar to get rid of sharp edges
            var downscaledAvatar = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
            var downscaledAvatarG = downscaledAvatar.createGraphics();
            downscaledAvatarG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            downscaledAvatarG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            downscaledAvatarG.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            downscaledAvatarG.drawImage(roundAvatar, 0, 0, AVATAR_SIZE, AVATAR_SIZE, null);
            downscaledAvatarG.dispose();

            // prepare level card
            String card = getPreferredCard(user);
            var rankCard = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            var g = rankCard.createGraphics();

            g.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);
            var background = RankingSystem.class.getResourceAsStream("/assets/wildcards/"+card+".png");
            if(background != null){
                var image = ImageIO.read(background);
                var width = image.getWidth();
                var height = image.getHeight();
                var drawWidth = 0;
                var drawHeight = 0;
                if(width > height){
                    drawWidth = width;
                    drawHeight = width / CARD_RATIO;
                }
                else{
                    drawHeight = height;
                    drawWidth = height * CARD_RATIO;
                }
                g.drawImage(image.getSubimage(0, 0, drawWidth, drawHeight), 0, 0, CARD_WIDTH, CARD_HEIGHT, null);
            }

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

            g.drawImage(downscaledAvatar, BORDER_SIZE, BORDER_SIZE, AVATAR_SIZE, AVATAR_SIZE, null);

            // draw username
            g.setFont(FONT.deriveFont(FONT_SIZE).deriveFont(Font.BOLD));
            g.setColor(Color.white);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            var userString = user.getName();
            var leftAvatarAlign = AVATAR_SIZE + BORDER_SIZE * 2;
            g.drawString(userString, leftAvatarAlign, CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT);

            // draw discriminator
            var nameWidth = g.getFontMetrics().stringWidth(userString);
            g.setFont(g.getFont().deriveFont(DISCRIMINATOR_FONT_SIZE).deriveFont(Font.BOLD));
            g.setColor(g.getColor().darker().darker());
            g.drawString("#" + user.getDiscriminator(), AVATAR_SIZE + BORDER_SIZE * 2 + nameWidth, CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT);

            // draw xp
            var totalXP = getTotalXP(guild.getIdLong(), user.getIdLong());
            var currentLevel = getLevel(totalXP);
            var neededXP = getXPToLevelUp(currentLevel);
            var currentXP = totalXP-getTotalXPNeeded(currentLevel);
            // draw level
            Color c = getColor(card);
            g.setFont(g.getFont().deriveFont(FONT_SIZE).deriveFont(Font.BOLD));
            g.setColor(Color.white);
            var levelString = "Level " + currentLevel;
            g.drawString(levelString, CARD_WIDTH - BORDER_SIZE - g.getFontMetrics().stringWidth(levelString), CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT);
            g.setColor(c);
            // draw empty xp bar
            g.setColor(c.darker().darker());
            g.fillRoundRect(leftAvatarAlign, CARD_HEIGHT - XP_BAR_HEIGHT - BORDER_SIZE, XP_BAR_WIDTH, XP_BAR_HEIGHT, XP_BAR_HEIGHT, XP_BAR_HEIGHT);

            // draw current xp bar
            g.setColor(c);
            g.fillRoundRect(leftAvatarAlign, CARD_HEIGHT - XP_BAR_HEIGHT - BORDER_SIZE, (int) (((double) currentXP) / neededXP * XP_BAR_WIDTH), XP_BAR_HEIGHT, XP_BAR_HEIGHT, XP_BAR_HEIGHT);

            g.setFont(g.getFont().deriveFont(FONT_SIZE/2).deriveFont(Font.BOLD));
            g.setColor(Color.white);
            var xpString = currentXP + " / " + neededXP + " XP";
            int xpXPos = ((leftAvatarAlign+CARD_WIDTH-BORDER_SIZE)/2)-(g.getFontMetrics().stringWidth(xpString)/2);
            g.drawString(xpString, xpXPos, CARD_HEIGHT-BORDER_SIZE-18);
            g.dispose();

            var baos = new ByteArrayOutputStream();
            ImageIO.write(rankCard, "png", baos);
            return baos.toByteArray();
        }
        catch(IOException e){
            LOGGER.error("Error while generating level cards", e);
        }
        return null;
    }

    public static String formatXP(int xp)
    {
        return null;
    }

    public static Color getColor(String fileName)
    {
        return Color.decode("#"+colorData.get(fileName, String.class));
    }

}
