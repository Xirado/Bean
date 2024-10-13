package at.xirado.bean.leveling

import at.xirado.bean.data.ResourceService
import at.xirado.bean.database.entity.Member
import at.xirado.bean.model.MemberExperienceStats
import org.koin.core.annotation.Single
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Path
import java.text.DecimalFormat

@Single
class RankCardStatsRenderer {
    private val font = ResourceService.getFile(Path.of("fonts/NotoSans.ttf")).let {
        Font.createFont(Font.TRUETYPE_FONT, it)
    }

    fun renderRankCardStats(member: Member, xpStats: MemberExperienceStats): BufferedImage {
        val stats = BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB)
        val graphics = stats.createGraphics()

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        graphics.color = Color.white
        graphics.fillArc(BORDER_SIZE, BORDER_SIZE, AVATAR_SIZE, AVATAR_SIZE, 0, 360)

        graphics.font = font.deriveFont(FONT_SIZE).deriveFont(Font.BOLD)

        val username = member.user.username
        val avatarAlign = AVATAR_SIZE + BORDER_SIZE * 2
        graphics.drawString(username, avatarAlign, CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT)

        val levelString = "Level ${xpStats.currentLevel}"

        graphics.drawString(
            levelString,
            CARD_WIDTH - BORDER_SIZE - graphics.fontMetrics.stringWidth(levelString),
            CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT
        )

        val accentColor = member.user.rankBackground?.color?.let { Color(it) }
            ?: DEFAULT_ACCENT_COLOR

        graphics.color = accentColor.darker().darker()

        graphics.fillRoundRect(
            avatarAlign,
            CARD_HEIGHT - XP_BAR_HEIGHT - BORDER_SIZE,
            XP_BAR_WIDTH,
            XP_BAR_HEIGHT,
            XP_BAR_HEIGHT,
            XP_BAR_HEIGHT
        )

        graphics.color = accentColor

        var progress = ((xpStats.relativeXp.toFloat() / xpStats.relativeXpToLevelUp) * XP_BAR_WIDTH).toInt()
        if (progress < 50 && progress != 0) progress = 50

        graphics.fillRoundRect(
            avatarAlign,
            CARD_HEIGHT - XP_BAR_HEIGHT - BORDER_SIZE,
            progress,
            XP_BAR_HEIGHT,
            XP_BAR_HEIGHT,
            XP_BAR_HEIGHT
        )

        val rank = xpStats.rank ?: -1
        val rankColor = getRankColor(rank) ?: accentColor.brighter()
        graphics.color = rankColor
        val rankString = "#$rank"
        val rankWidth = graphics.fontMetrics.stringWidth(rankString)
        graphics.drawString(rankString, CARD_WIDTH - BORDER_SIZE - rankWidth, 70)

        graphics.font = graphics.font.deriveFont(DISCRIMINATOR_FONT_SIZE - 5).deriveFont(Font.BOLD)
        val rankStringWidth = graphics.fontMetrics.stringWidth("Rank") + 10
        graphics.drawString("Rank", CARD_WIDTH - BORDER_SIZE - rankWidth - rankStringWidth, 70)

        graphics.font = graphics.font.deriveFont(FONT_SIZE / 2).deriveFont(Font.BOLD)
        graphics.color = Color.white
        val xpString = "${formatXP(xpStats.relativeXp)} / ${formatXP(xpStats.relativeXpToLevelUp)}"
        val xpXPos = ((avatarAlign + CARD_WIDTH - BORDER_SIZE) / 2) - (graphics.fontMetrics.stringWidth(xpString) / 2)
        graphics.drawString(xpString, xpXPos, CARD_HEIGHT - BORDER_SIZE - 18)
        graphics.dispose()

        return stats
    }

    private fun getRankColor(rank: Int): Color? {
        return when (rank) {
            1 -> Color.decode("#D4AF37")
            2 -> Color.decode("#BEC2CB")
            3 -> Color.decode("#CD7F32")
            else -> null
        }
    }
}

private val suffix = arrayOf("", "k", "M", "G", "T")
private const val MAX_LENGTH = 5

fun formatXP(xp: Int): String {
    var r = DecimalFormat("##0E0").format(xp)
    r = r.replace("E[0-9]".toRegex(), suffix[Character.getNumericValue(r[r.length - 1]) / 3])
    while (r.length > MAX_LENGTH || r.matches("[0-9]+,[a-z]".toRegex())) {
        r = r.substring(0, r.length - 2) + r.substring(r.length - 1)
    }
    return r.replace(",", ".")
}