package at.xirado.bean.manager

import at.xirado.bean.Application
import at.xirado.bean.coroutineScope
import at.xirado.bean.executor
import at.xirado.bean.interaction.command.slash.rank.imageDirectory
import at.xirado.bean.io.db.SQLBuilder
import at.xirado.bean.util.retrieveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.text.DecimalFormat
import java.util.concurrent.Callable
import javax.imageio.ImageIO

private const val CARD_WIDTH = 1200
private const val CARD_HEIGHT = CARD_WIDTH / 4
private const val CARD_RATIO = CARD_WIDTH / CARD_HEIGHT

private const val FONT_SIZE = 60f
private const val DISCRIMINATOR_FONT_SIZE = (FONT_SIZE / 1.5f)
private const val RANK_FONT_SIZE = (DISCRIMINATOR_FONT_SIZE / 1.5f)

private const val RAW_AVATAR_SIZE = 512
private const val RAW_AVATAR_BORDER_SIZE = RAW_AVATAR_SIZE / 64

private const val BORDER_SIZE = CARD_HEIGHT / 13

private const val AVATAR_SIZE = CARD_HEIGHT - BORDER_SIZE * 2

private const val XP_BAR_WIDTH = CARD_WIDTH - AVATAR_SIZE - BORDER_SIZE * 3
private const val XP_BAR_HEIGHT = CARD_HEIGHT / 5

private val RESCALE_OP = RescaleOp(.5f, 0f, null)
private val FONT = Font.createFont(Font.TRUETYPE_FONT, Application::class.java.getResourceAsStream("/assets/fonts/NotoSans.ttf"))

context(Application)
suspend fun generateCard(user: User, guild: Guild): ByteArray {
    val avatarDeferred = coroutineScope.async(context = Dispatchers.IO) { ImageIO.read(URL(user.effectiveAvatarUrl + "?size=" + RAW_AVATAR_SIZE)) }
    val backgroundDeferred = coroutineScope.async(context = Dispatchers.IO) { getUserBackground(user) }

    val avatar = avatarDeferred.await()
    val background = backgroundDeferred.await()

    val accentColor = user.retrieveData().rankCardConfig.accentColor
    val xpTotal = retrieveTotalExperience(guild.idLong, user.idLong)
    val rank = retrieveRank(user.idLong, guild.idLong)

    val callable = Callable { drawImage(avatar, background, user, accentColor, xpTotal, rank) }

    return withContext(Dispatchers.IO) {
        val (time, card) = measureTimeMillisWithResult { executor.submit(callable).get() }
        println("$time ms")
        return@withContext card
    }
}

fun getExperienceToLevelUp(currentLevel: Int) = 5 * (currentLevel * currentLevel) + (50 * currentLevel) + 100

fun getLevel(experience: Int): Int {
    if (experience < 100) return 0
    var counter = 0
    var total = 0
    while (true) {
        val neededForNextLevel = getExperienceToLevelUp(counter)
        if (neededForNextLevel > experience) return counter
        total += neededForNextLevel
        if (total > experience) return counter
        counter++
    }
}

fun getTotalExperienceNeeded(level: Int) = (0 until level).map(::getExperienceToLevelUp).sum()

suspend fun retrieveTotalExperience(guildId: Long, userId: Long): Int {
    val query = SQLBuilder("SELECT experience.experience FROM experience WHERE guild_id = ? and user_id = ?", guildId, userId)

    return query.executeQuery {
        if (it.next())
            return@executeQuery it.getInt("experience")
        return@executeQuery 0
    }!!
}

suspend fun retrieveRank(userId: Long, guildId: Long): Int {
    return SQLBuilder("SELECT * FROM (SELECT RANK() over(order by experience desc), user_id from experience where guild_id = ?) as rank where user_id = ?",
        guildId, userId).executeQuery {
            if (it.next())
                return@executeQuery it.getInt("rank")
        return@executeQuery -1
    }!!
}

suspend fun addExperience(guildId: Long, userId: Long, addedAmount: Int, name: String, discriminator: String) {
    setExperience(guildId, userId, retrieveTotalExperience(guildId, userId) + addedAmount, name, discriminator)
}

suspend fun setExperience(guildId: Long, userId: Long, newAmount: Int, name: String, discriminator: String) {
    val query = SQLBuilder("INSERT INTO experience (guild_id, user_id, experience, name, discriminator) values (?,?,?,?,?) " +
            "ON CONFLICT(guild_id, user_id) DO UPDATE SET experience = excluded.experience, name = excluded.name, discriminator = excluded.discriminator")

    query.addParameter(guildId, userId, newAmount, name, discriminator)
    query.execute()
}

context(Application)
private suspend fun getUserBackground(user: User): BufferedImage {
    val background = user.retrieveData().rankCardConfig.background

    return if (background == "default") {
        getDefaultBackground()
    } else {
        withContext(Dispatchers.IO) {
            val file = File(imageDirectory, background)
            if (!file.exists())
                getDefaultBackground()
            else
                ImageIO.read(FileInputStream(file))
        }
    }
}

private suspend fun getDefaultBackground() = withContext(Dispatchers.IO) {
    ImageIO.read(Application::class.java.getResourceAsStream("/assets/wildcards/default.jpg"))
}

private fun drawImage(avatar: BufferedImage, background: BufferedImage, user: User, accentColor: Int, experience: Int, rank: Int): ByteArray {
    val roundAvatar = BufferedImage(RAW_AVATAR_SIZE, RAW_AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB)
    roundAvatar.createGraphics().apply {
        color = Color.white
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
        fillArc(0, 0, RAW_AVATAR_SIZE, RAW_AVATAR_SIZE, 0, 360)
        val rawAvatarSize = RAW_AVATAR_SIZE - RAW_AVATAR_BORDER_SIZE * 2
        clip = Ellipse2D.Float(RAW_AVATAR_BORDER_SIZE.toFloat(), RAW_AVATAR_BORDER_SIZE.toFloat(), rawAvatarSize.toFloat(), rawAvatarSize.toFloat())
        drawImage(avatar, RAW_AVATAR_BORDER_SIZE, RAW_AVATAR_BORDER_SIZE, rawAvatarSize, rawAvatarSize, null)
        dispose()
    }

    val downScaledAvatar = BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB)
    downScaledAvatar.createGraphics().apply {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
        drawImage(roundAvatar, 0, 0, AVATAR_SIZE, AVATAR_SIZE, null)
        dispose()
    }

    val rankCard = BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB)
    val g = rankCard.createGraphics()

    val backgroundImage = RESCALE_OP.filter(background, null)

    var width = backgroundImage.width
    var height = backgroundImage.height
    if (width > 1200)
        width = 1200

    if (height > 300)
        height = 300

    var drawWidth: Int
    var drawHeight: Int

    if (width > height) {
        drawWidth = width
        drawHeight = width / CARD_RATIO
    } else {
        drawHeight = height
        drawWidth = height * CARD_RATIO
    }

    if (drawWidth > width)
        drawWidth = width

    if (drawHeight > height)
        drawHeight = height

    g.drawImage(backgroundImage.getSubimage(0, 0, drawWidth, drawHeight), 0, 0, CARD_WIDTH, CARD_HEIGHT, null)

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)

    g.drawImage(downScaledAvatar, BORDER_SIZE, BORDER_SIZE, AVATAR_SIZE, AVATAR_SIZE, null)

    g.font = FONT.deriveFont(FONT_SIZE).deriveFont(Font.BOLD)
    g.color = Color.white
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    val userString = user.name
    val leftAvatarAlign = AVATAR_SIZE + BORDER_SIZE * 2
    g.drawString(userString, leftAvatarAlign, CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT)

    val c = Color(accentColor)

    g.color = g.color.darker()

    val nameWidth = g.fontMetrics.stringWidth(userString)
    g.font = g.font.deriveFont(DISCRIMINATOR_FONT_SIZE).deriveFont(Font.BOLD)
    val discriminator = "#${user.discriminator}"
    val discriminatorSize = g.fontMetrics.getStringBounds(discriminator, g)
//    val discriminatorWidth = discriminatorSize.width.toInt()
    g.drawString(discriminator, AVATAR_SIZE + BORDER_SIZE * 2 + nameWidth, CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT)

//    if (userRank != null) {
//        g.font = g.font.deriveFont(RANK_FONT_SIZE).deriveFont(Font.BOLD)
//        val addedSize = AVATAR_SIZE + BORDER_SIZE * 2 + nameWidth + discriminatorWidth
//        g.color = Color(userRank.color)
//        val padding = 10
//        val rankSize = g.fontMetrics.getStringBounds(userRank.display, g)
//        g.fillRoundRect(addedSize + 10, CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT - (rankSize.height.toInt()) + 4, rankSize.width.toInt() + (2 * padding), rankSize.height.toInt(), rankSize.height.toInt(), rankSize.height.toInt())
//        g.color = Color.white
//        g.drawString(userRank.display, addedSize + padding + 10, CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT - 4)
//    }

    val currentLevel = getLevel(experience)
    val neededXP = getExperienceToLevelUp(currentLevel)
    val currentXP = experience - getTotalExperienceNeeded(currentLevel)

    g.font = g.font.deriveFont(FONT_SIZE).deriveFont(Font.BOLD)
    g.color = Color.white
    val levelString = "Level $currentLevel"
    g.drawString(levelString, CARD_WIDTH - BORDER_SIZE - g.fontMetrics.stringWidth(levelString), CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT)
    g.color = c

    g.color = c.darker().darker()
    g.fillRoundRect(leftAvatarAlign, CARD_HEIGHT - XP_BAR_HEIGHT - BORDER_SIZE, XP_BAR_WIDTH, XP_BAR_HEIGHT, XP_BAR_HEIGHT, XP_BAR_HEIGHT)

    g.color = c
    var progress = (currentXP.toDouble() / neededXP * XP_BAR_WIDTH).toInt()
    if (progress < 50 && progress != 0) progress = 50
    g.fillRoundRect(
        leftAvatarAlign,
        CARD_HEIGHT - XP_BAR_HEIGHT - BORDER_SIZE,
        progress,
        XP_BAR_HEIGHT,
        XP_BAR_HEIGHT,
        XP_BAR_HEIGHT
    )
    val rankColor = getRankColor(rank)
    g.color = rankColor ?: c.brighter()
    val rankString = "#$rank"
    val rankWidth = g.fontMetrics.stringWidth(rankString)
    g.drawString(rankString, CARD_WIDTH - BORDER_SIZE - rankWidth, 70)
    g.font = g.font.deriveFont(DISCRIMINATOR_FONT_SIZE - 5).deriveFont(Font.BOLD)
    g.drawString("Rank", CARD_WIDTH - BORDER_SIZE - rankWidth - g.fontMetrics.stringWidth("Rank") - 10, 70)
    g.font = g.font.deriveFont(FONT_SIZE / 2).deriveFont(Font.BOLD)
    g.color = Color.white
    val xpString: String = (formatXP(currentXP) + " / " + formatXP(neededXP)) + " XP"
    val xpXPos = (leftAvatarAlign + CARD_WIDTH - BORDER_SIZE) / 2 - g.fontMetrics.stringWidth(xpString) / 2
    g.drawString(xpString, xpXPos, CARD_HEIGHT - BORDER_SIZE - 18)
    g.dispose()

    val outputStream = ByteArrayOutputStream()
    ImageIO.write(makeRoundedCorner(rankCard, 60), "png", outputStream)
    return outputStream.toByteArray()
}

private val suffixes = arrayOf("", "k", "M", "G", "T")
private const val maxLength = 5

fun formatXP(xp: Int): String {
    var r: String = DecimalFormat("##0E0").format(xp)
    r = r.replace("E[0-9]".toRegex(), suffixes[Character.getNumericValue(r[r.length - 1]) / 3])
    while (r.length > maxLength || r.matches("[0-9]+,[a-z]".toRegex())) {
        r = r.substring(0, r.length - 2) + r.substring(r.length - 1)
    }
    return r.replace(",".toRegex(), ".")
}

fun makeRoundedCorner(image: BufferedImage, cornerRadius: Int): BufferedImage {
    val w = image.width
    val h = image.height
    val output = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g2 = output.createGraphics()

    g2.composite = AlphaComposite.Src
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.color = Color.WHITE
    g2.fill(RoundRectangle2D.Float(0f, 0f, w.toFloat(), h.toFloat(), cornerRadius.toFloat(), cornerRadius.toFloat()))

    g2.composite = AlphaComposite.SrcIn
    g2.drawImage(image, 0, 0, null)
    g2.dispose()
    return output
}

fun getRankColor(rank: Int): Color? {
    return when (rank) {
        1 -> Color.decode("#D4AF37")
        2 -> Color.decode("#BEC2CB")
        3 -> Color.decode("#CD7F32")
        else -> null
    }
}