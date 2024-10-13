package at.xirado.bean.leveling

import java.awt.Color

const val CARD_WIDTH = 1200
const val CARD_HEIGHT = CARD_WIDTH / 4
const val CARD_RATIO = CARD_WIDTH / CARD_HEIGHT

const val FONT_SIZE = 60f
const val DISCRIMINATOR_FONT_SIZE = FONT_SIZE / 1.5f

const val RAW_AVATAR_SIZE = 512
const val RAW_AVATAR_BORDER_SIZE = RAW_AVATAR_SIZE / 64

const val BORDER_SIZE = CARD_HEIGHT / 13

const val AVATAR_SIZE = CARD_HEIGHT - BORDER_SIZE * 2

const val XP_BAR_WIDTH = CARD_WIDTH - AVATAR_SIZE - BORDER_SIZE * 3
const val XP_BAR_HEIGHT = CARD_HEIGHT / 5

val DEFAULT_ACCENT_COLOR = Color(0x0C71E0)