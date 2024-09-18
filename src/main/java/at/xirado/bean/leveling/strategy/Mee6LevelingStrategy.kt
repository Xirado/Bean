package at.xirado.bean.leveling.strategy

object Mee6LevelingStrategy : LevelingStrategy {
    override fun getExperienceToLevelUp(currentLevel: Int): Int {
        val experience = 5 * (currentLevel * currentLevel) + (50 * currentLevel) + 100
        return experience
    }
}