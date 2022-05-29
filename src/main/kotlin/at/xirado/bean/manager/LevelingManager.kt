package at.xirado.bean.manager

import at.xirado.bean.Application
import at.xirado.bean.io.db.SQLBuilder
import java.awt.Font

class LevelingManager(private val application: Application) {
    private val font = Font.createFont(Font.TRUETYPE_FONT, this::class.java.getResourceAsStream("/assets/fonts/NotoSans.ttf"))

}

private const val CARD_WIDTH = 1200
private const val CARD_HEIGHT = CARD_WIDTH / 4
private const val CARD_RATIO = CARD_WIDTH / CARD_HEIGHT

private const val FONT_SIZE = 60f
private const val DISCRIMINATOR_FONT_SIZE = (FONT_SIZE / 1.5f)

private const val RAW_AVATAR_SIZE = 512
private const val RAW_AVATAR_BORDER_SIZE = RAW_AVATAR_SIZE / 64

private const val BORDER_SIZE = CARD_HEIGHT / 13

private const val AVATAR_SIZE = CARD_HEIGHT - BORDER_SIZE * 2

private const val XP_BAR_WIDTH = CARD_WIDTH - AVATAR_SIZE - BORDER_SIZE * 3
private const val XP_BAR_HEIGHT = CARD_HEIGHT / 5

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
    val query = SQLBuilder("SELECT xp_total FROM levels WHERE guild_id = ? and user_id = ?", guildId, userId)

    return query.executeQuery {
        if (it.next())
            return@executeQuery it.getInt("xp_total")
        return@executeQuery 0
    }!!
}

suspend fun addExperience(guildId: Long, userId: Long, addedAmount: Int, name: String, discriminator: String) {
    setExperience(guildId, userId, retrieveTotalExperience(guildId, userId) + addedAmount, name, discriminator)
}

suspend fun setExperience(guildId: Long, userId: Long, newAmount: Int, name: String, discriminator: String) {
    val query = SQLBuilder("INSERT INTO levels (guild_id, user_id, xp_total, name, discriminator) values (?,?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE xp_total = VALUES(xp_total), name = VALUES(name), discriminator = VALUES(discriminator)")

    query.addParameter(guildId, userId, newAmount, name, discriminator)
    query.execute()
}