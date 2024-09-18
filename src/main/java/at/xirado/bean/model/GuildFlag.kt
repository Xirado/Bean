package at.xirado.bean.model

import at.xirado.bean.data.BitField

enum class GuildFlag(override val offset: Int, val identifier: String) : BitField {
    DEV_GUILD(0, "Development Guild"),
}