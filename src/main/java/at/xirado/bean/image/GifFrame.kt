package at.xirado.bean.image

import java.awt.image.BufferedImage

data class GifFrame(
    val image: BufferedImage,
    val duration: Int,
)