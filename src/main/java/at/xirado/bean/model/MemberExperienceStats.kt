package at.xirado.bean.model

data class MemberExperienceStats(
    val experience: Int,
    val currentLevel: Int,
    val relativeXpToLevelUp: Int,
    val relativeXp: Int,
    val rank: Int?,
)