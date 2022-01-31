package at.xirado.bean.data;

import at.xirado.bean.data.database.Database;
import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.misc.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class RankingSystem
{

    private static final Logger LOGGER = LoggerFactory.getLogger(RankingSystem.class);
    private static LinkedDataObject colorData = null;
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

    static
    {
        try
        {
            FONT = Font.createFont(Font.TRUETYPE_FONT, RankingSystem.class.getResourceAsStream("/assets/fonts/NotoSans.ttf"));
            colorData = LinkedDataObject.parse(RankingSystem.class.getResourceAsStream("/assets/wildcards/ColorInfo.json"));
        } catch (FontFormatException | IOException e)
        {
            LOGGER.error("Couldn't load font from resources", e);
        }
    }

    /**
     * returns the relative XP needed to level up to the next level
     *
     * @param currentLevel the current level
     * @return relative XP needed to level up
     */
    public static long getXPToLevelUp(int currentLevel)
    {
        double x = 5 * (currentLevel * currentLevel) + (50 * currentLevel) + 100;
        return (long) x;
    }

    /**
     * returns the current level
     *
     * @param xp total xp
     * @return the level
     */
    public static int getLevel(long xp)
    {
        if (xp < 100) return 0;
        int counter = 0;
        long total = 0L;
        while (true)
        {
            long neededForNextLevel = getXPToLevelUp(counter);
            if (neededForNextLevel > xp) return counter;
            total += neededForNextLevel;
            if (total > xp) return counter;
            counter++;
        }
    }

    /**
     * returns the total xp needed to reach a certain level
     *
     * @param level the level
     * @return total xp needed to reach that level
     */
    public static long getTotalXPNeeded(int level)
    {
        long x = 0;
        for (int i = 0; i < level; i++)
        {
            x += getXPToLevelUp(i);
        }
        return x;
    }

    public static long getTotalXP(@Nonnull Connection connection, long guildID, long userID)
    {
        var query = new SQLBuilder("SELECT totalXP FROM levels WHERE guildID = ? AND userID = ?")
                .useConnection(connection)
                .addParameters(guildID, userID);
        try (var result = query.executeQuery())
        {
            if (result.next())
                return result.getLong("totalXP");
            return 0L;
        } catch (Exception ex)
        {
            LOGGER.error("Could not get total xp! (guild " + guildID + ", user " + userID + ")", ex);
            return -1L;
        }
    }

    public static long getTotalXP(long guildID, long userID)
    {
        var query = new SQLBuilder("SELECT totalXP FROM levels WHERE guildID = ? AND userID = ?")
                .addParameters(guildID, userID);
        try (var result = query.executeQuery())
        {
            if (result.next())
                return result.getLong("totalXP");
            return 0L;
        } catch (Exception ex)
        {
            LOGGER.error("Could not get total xp! (guild " + guildID + ", user " + userID + ")", ex);
            return -1L;
        }
    }

    public static void addXP(long guildID, long userID, long addedAmount, String name, String discriminator)
    {
        var connection = Database.getConnectionFromPool();
        if (connection == null)
        {
            LOGGER.error("Could not get connection from db pool!", new SQLException("Connection == null!"));
            return;
        }
        var sql = "INSERT INTO levels (guildID, userID, totalXP, name, discriminator) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE totalXP = ?, name = ?, discriminator = ?";
        var totalXP = getTotalXP(connection, guildID, userID) + addedAmount;
        var query = new SQLBuilder(sql)
                .useConnection(connection)
                .addParameters(guildID, userID, totalXP, name, discriminator, totalXP, name, discriminator);
        try
        {
            query.execute();
        } catch (SQLException e)
        {
            LOGGER.error("Could not add XP!", e);
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static void addXP(@Nonnull Connection connection, long guildID, long userID, long addedAmount, String name, String discriminator, String avatarUrl)
    {
        var sql = "INSERT INTO levels (guildID, userID, totalXP, name, discriminator, avatar) values (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE totalXP = ?, name = ?, discriminator = ?, avatar = ?";
        var totalXP = getTotalXP(connection, guildID, userID) + addedAmount;
        var query = new SQLBuilder(sql)
                .useConnection(connection)
                .addParameters(guildID, userID, totalXP, name, discriminator, avatarUrl, totalXP, name, discriminator, avatarUrl);
        try
        {
            query.execute();
        } catch (SQLException e)
        {
            LOGGER.error("Could not add XP!", e);
        }
    }

    public static void setXP(long guildID, long userID, long setAmount, String name, String discriminator)
    {
        var sql = "INSERT INTO levels (guildID, userID, totalXP, name, discriminator) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE totalXP = ?, name = ?, discriminator = ?";
        var query = new SQLBuilder(sql)
                .addParameters(guildID, userID, setAmount, name, discriminator, setAmount, name, discriminator);
        try
        {
            query.execute();
        } catch (SQLException e)
        {
            LOGGER.error("Could not add XP!", e);
        }
    }

    public static void setXP(@Nonnull Connection connection, long guildID, long userID, long setAmount, String name, String discriminator)
    {
        var sql = "INSERT INTO levels (guildID, userID, totalXP, name, discriminator) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE totalXP = ?, name = ?, discriminator = ?";
        var query = new SQLBuilder(sql)
                .useConnection(connection)
                .addParameters(guildID, userID, setAmount, name, discriminator, setAmount, name, discriminator);
        try
        {
            query.execute();
        } catch (SQLException e)
        {
            LOGGER.error("Could not add XP!", e);
        }
    }

    public static String getPreferredCard(@Nonnull User user)
    {
        var query = new SQLBuilder("SELECT * FROM wildcardSettings WHERE userID = ?")
                .addParameter(user.getIdLong());
        try (var rs = query.executeQuery())
        {
            return rs.next() ? rs.getString("card") : "card1";
        } catch (SQLException ex)
        {
            LOGGER.error("Could not get user preferred wildcard background (user " + user.getIdLong() + ")", ex);
            return "card1";
        }
    }

    public static void setPreferredCard(@Nonnull User user, @Nonnull String card)
    {
        String qry = "INSERT INTO wildcardSettings (userID, card) VALUES (?,?) ON DUPLICATE KEY UPDATE card = ?";
        var query = new SQLBuilder(qry)
                .addParameters(user.getIdLong(), card, card);
        try
        {
            query.execute();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not set user preferred wildcard background (user " + user.getIdLong() + ")", ex);
        }
    }

    public static String getPreferredCard(@Nonnull Connection connection, @Nonnull User user)
    {
        var query = new SQLBuilder("SELECT * FROM wildcardSettings WHERE userID = ?")
                .useConnection(connection)
                .addParameter(user.getIdLong());
        try (var rs = query.executeQuery())
        {
            return rs.next() ? rs.getString("card") : "card1";
        } catch (SQLException ex)
        {
            LOGGER.error("Could not get user preferred wildcard background (user " + user.getIdLong() + ")", ex);
            return "card1";
        }
    }

    public static void setPreferredCard(@Nonnull Connection connection, @Nonnull User user, @Nonnull String card)
    {
        String qry = "INSERT INTO wildcardSettings (userID, card) VALUES (?,?) ON DUPLICATE KEY UPDATE card = ?";
        var query = new SQLBuilder(qry)
                .useConnection(connection)
                .addParameters(user.getIdLong(), card, card);
        try
        {
            query.execute();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not set user preferred wildcard background (user " + user.getIdLong() + ")", ex);
        }
    }

    public static byte[] generateLevelCard(@Nonnull User user, @Nonnull Guild guild)
    {
        try
        {
            var avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl() + "?size=" + RAW_AVATAR_SIZE));

            // make the avatar round
            var roundAvatar = new BufferedImage(RAW_AVATAR_SIZE, RAW_AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
            var roundAvatarG = roundAvatar.createGraphics();
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

            var background = RankingSystem.class.getResourceAsStream("/assets/wildcards/" + card + ".png");
            if (background != null)
            {
                var image = makeRoundedCorner(ImageIO.read(background), 60);
                var width = image.getWidth();
                var height = image.getHeight();
                var drawWidth = 0;
                var drawHeight = 0;
                if (width > height)
                {
                    drawWidth = width;
                    drawHeight = width / CARD_RATIO;
                } else
                {
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
            var currentXP = totalXP - getTotalXPNeeded(currentLevel);
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
            int progress = (int) (((double) currentXP) / neededXP * XP_BAR_WIDTH);
            if (progress < 50 && progress != 0) progress = 50;
            g.fillRoundRect(leftAvatarAlign, CARD_HEIGHT - XP_BAR_HEIGHT - BORDER_SIZE, progress, XP_BAR_HEIGHT, XP_BAR_HEIGHT, XP_BAR_HEIGHT);
            int rank = getRank(guild.getIdLong(), user.getIdLong());
            Color rankColor = getRankColor(rank);
            g.setColor(rankColor == null ? c.brighter() : rankColor);
            String rankString = "#" + rank;
            int rankWidth = g.getFontMetrics().stringWidth(rankString);
            g.drawString(rankString, CARD_WIDTH - BORDER_SIZE - rankWidth, 70);
            g.setFont(g.getFont().deriveFont(DISCRIMINATOR_FONT_SIZE - 5).deriveFont(Font.BOLD));
            g.drawString("Rank", CARD_WIDTH - BORDER_SIZE - rankWidth - g.getFontMetrics().stringWidth("Rank") - 10, 70);
            g.setFont(g.getFont().deriveFont(FONT_SIZE / 2).deriveFont(Font.BOLD));
            g.setColor(Color.white);
            var xpString = formatXP(currentXP) + " / " + formatXP(neededXP) + " XP";
            int xpXPos = ((leftAvatarAlign + CARD_WIDTH - BORDER_SIZE) / 2) - (g.getFontMetrics().stringWidth(xpString) / 2);
            g.drawString(xpString, xpXPos, CARD_HEIGHT - BORDER_SIZE - 18);
            g.dispose();

            var baos = new ByteArrayOutputStream();
            ImageIO.write(rankCard, "png", baos);
            return baos.toByteArray();
        } catch (IOException e)
        {
            LOGGER.error("Error while generating level cards", e);
        }
        return null;
    }

    private static final String[] suffix = new String[]{"", "k", "M", "G", "T"};
    private static final int MAX_LENGTH = 5;

    public static String formatXP(long xp)
    {
        String r = new DecimalFormat("##0E0").format(xp);
        r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
        while (r.length() > MAX_LENGTH || r.matches("[0-9]+,[a-z]"))
        {
            r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
        }
        return r.replaceAll(",", ".");
    }

    public static Color getColor(String fileName)
    {
        return Color.decode("#" + colorData.get(fileName, String.class));
    }

    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius)
    {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    public static DataArray getLeaderboard(long guildId, int page, int itemsPerPage) throws SQLException
    {
        var start = page == 1 ? 0 : ((page - 1) * itemsPerPage);
        var query = new SQLBuilder("SELECT * FROM levels WHERE guildID = ? ORDER by totalXP DESC LIMIT ?, ?", guildId, start, itemsPerPage);
        try (var rs = query.executeQuery())
        {
            DataArray array = DataArray.empty();
            while (rs.next())
            {
                array.add(
                        DataObject.empty()
                                .put("user", rs.getLong("userID"))
                                .put("xp", rs.getLong("totalXP"))
                                .put("name", rs.getString("name"))
                                .put("discriminator", rs.getString("discriminator"))
                                .put("avatar", rs.getString("avatar"))
                );
            }
            return array;
        }
    }

    public static int getDataCount(long guildId) throws SQLException
    {
        var query = new SQLBuilder("SELECT COUNT(*) FROM levels WHERE guildID = ?", guildId);
        try (var rs = query.executeQuery())
        {
            if (rs.next())
                return rs.getInt("COUNT(*)");
            return 0;
        }
    }

    public static int getRank(long guildID, long userID)
    {
        String qry = "SELECT totalXP, FIND_IN_SET( totalXP, ( SELECT GROUP_CONCAT( totalXP ORDER BY totalXP DESC ) FROM levels WHERE guildID = ? )) AS rank FROM levels WHERE guildID = ? and userID = ?";
        var query = new SQLBuilder(qry)
                .addParameters(guildID, guildID, userID);
        try (var rs = query.executeQuery())
        {
            if (rs.next()) return rs.getInt("rank");
            return -1;
        } catch (SQLException ex)
        {
            LOGGER.error("Could not get rank of member! Guild: {} User: {}", guildID, userID);
            return -1;
        }
    }

    public static Color getRankColor(int rank)
    {
        return switch (rank)
                {
                    case 1 -> Color.decode("#D4AF37");
                    case 2 -> Color.decode("#BEC2CB");
                    case 3 -> Color.decode("#CD7F32");
                    default -> null;
                };
    }
}
