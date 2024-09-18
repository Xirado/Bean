package at.xirado.bean.model

import at.xirado.bean.data.BitField

enum class UserFlag(override val offset: Int, val identifier: String) : BitField {
    ADMIN(1, "Administrator"),
    VIP(0, "VIP"),
}