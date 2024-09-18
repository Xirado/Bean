package at.xirado.bean.leveling.strategy

interface LevelingStrategy {
    fun getLevel(experience: Int): Int {
        var counter = 0
        var total = 0

        while (total <= experience) {
            val neededForNextLevel = Mee6LevelingStrategy.getExperienceToLevelUp(counter)
            if (total + neededForNextLevel > experience) return counter
            total += neededForNextLevel
            counter++
        }
        return counter
    }

    fun getExperienceNeededToReachLevel(level: Int): Int {
        return (0 until level).sumOf { getExperienceToLevelUp(it) }
    }

    fun getExperienceToLevelUp(currentLevel: Int): Int
}