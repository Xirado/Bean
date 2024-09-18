package at.xirado.bean.model

import at.xirado.bean.data.BitField

enum class GuildFeature(override val offset: Int, val identifier: String) : BitField {
    LEVELING(0, "Leveling")
}