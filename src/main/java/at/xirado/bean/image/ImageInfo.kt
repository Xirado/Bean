package at.xirado.bean.image

data class ImageInfo(
    val images: List<GifFrame>,
    val isAnimated: Boolean,
)

val ImageInfo.maxFramerate: Double
    get() = 1000.0 / images.minOf { it.duration }

val ImageInfo.minFrameDuration: Int
    get() = images.minOf { it.duration }

val ImageInfo.totalDuration: Int
    get() = images.sumOf { it.duration }

fun ImageInfo.getFrameAtPosition(positionMillis: Int): GifFrame {
    if (positionMillis < 0) {
        throw IllegalArgumentException("Cannot get frame at negative position ($positionMillis)")
    }

    val totalDuration = totalDuration
    if (totalDuration == 0) {
        return images.first()
    }

    if (positionMillis == totalDuration)
        return images.last()

    val loopedPositionMillis = positionMillis % totalDuration

    var cumulativeDuration = 0

    for (frame in images) {
        cumulativeDuration += frame.duration
        if (loopedPositionMillis < cumulativeDuration) {
            return frame
        }
    }

    return images.last()
}

fun ImageInfo.getFrameAtPositionOrLastIfCutOff(positionMillis: Int, cutOff: Int): GifFrame {
    val totalDuration = totalDuration // Get the total duration of the GIF

    if (positionMillis < totalDuration) {
        return getFrameAtPosition(positionMillis)
    }

    val loops = cutOff / totalDuration
    val maxDurationWithLoops = loops * totalDuration

    return if (positionMillis > maxDurationWithLoops) {
        images.last()
    } else {
        getFrameAtPosition(positionMillis % totalDuration)
    }
}