package at.xirado.bean.image

import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

data class OverlayedAnimation(val order: List<Pair<BufferedImage, BufferedImage>>, val framerate: Double)

fun overlayAnimatedPictures(
    gif1: ImageInfo,
    gif2: ImageInfo,
): OverlayedAnimation {
    val increments = min(
        gif1.minFrameDuration,
        min(gif2.minFrameDuration, 30)
    )

    val gif1Duration = gif1.totalDuration
    val gif2Duration = gif2.totalDuration

    val duration = max(gif1Duration, gif2Duration)
    val framerate = 1000.0 / increments

    val frames = mutableListOf<Pair<BufferedImage, BufferedImage>>()

    var currentPosition = 0

    while (currentPosition < duration) {
        val frameGif1 = gif1.getFrameAtPositionOrLastIfCutOff(currentPosition, duration)
        val frameGif2 = gif2.getFrameAtPositionOrLastIfCutOff(currentPosition, duration)

        frames += frameGif1.image to frameGif2.image
        currentPosition += increments
    }

    return OverlayedAnimation(frames, framerate)
}