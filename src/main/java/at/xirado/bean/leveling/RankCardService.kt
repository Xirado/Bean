package at.xirado.bean.leveling

import at.xirado.bean.data.DBContext
import at.xirado.bean.data.ResourceService
import at.xirado.bean.data.cache.MemberCacheView
import at.xirado.bean.data.cache.UserCacheView
import at.xirado.bean.database.entity.User
import at.xirado.bean.image.GifFrame
import at.xirado.bean.image.ImageInfo
import com.github.benmanes.caffeine.cache.Caffeine
import com.madgag.gif.fmsware.GifDecoder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.utils.FileUpload
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.SeekableByteArrayOutputStream
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import net.dv8tion.jda.api.entities.Member as JDAMember
import net.dv8tion.jda.api.entities.User as JDAUser

private val backgroundDir = Path.of("backgrounds")
private val log = KotlinLogging.logger { }

private val animatedImageExtensions = arrayOf("gif", "webp")

@Single(createdAtStart = true)
class RankCardService(
    private val userCacheView: UserCacheView,
    private val memberCacheView: MemberCacheView,
    private val db: DBContext,
    private val stateRenderer: RankCardStatsRenderer,
//    private val config: Config,
) : KoinComponent {
    private val avatarCache = Caffeine.newBuilder()
        .maximumSize(100)
        .build<Long, ImageInfo>()
    private val backgroundCache = Caffeine.newBuilder()
        .maximumSize(100)
        .build<Long, ImageInfo>()
    private val defaultBackground: ImageInfo = getDefaultBackground()
    private val rescaleOp = RescaleOp(.5f, 0f, null)
//    private val executor = Executors.newFixedThreadPool(8)
//    private val dispatcher = executor.asCoroutineDispatcher()
//    private val coroutineScope = newCoroutineScope<RankCardService>(dispatcher)

    suspend fun generateRankCard(jdaMember: JDAMember): FileUpload = db.doTransaction {
        val member = memberCacheView.save(jdaMember)
        val xpStats = member.getExperienceStats(withRank = true)

        val background = getBackground(member.user)
        val stats = stateRenderer.renderRankCardStats(member, xpStats)
        val avatar = getAvatar(jdaMember.user)

        val rankCard = renderRankCardFrame(
            stats,
            background.images.first().image,
            avatar.images.first().image
        )

        FileUpload.fromData(rankCard.toByteArray(), "card.png")
//        val combinedFrames = overlayAnimatedPictures(avatar, background)
//
//        val renderedFrames = with (coroutineScope) {
//            combinedFrames.order.mapAsync { (avatar, background) ->
//                renderRankCardFrame(stats, background, avatar)
//            }
//        }.awaitAll()
//
//        val image = createAnimatedWebp(renderedFrames, combinedFrames.framerate)
//        FileUpload.fromData(image, "card.webp")
    }

    private fun renderRankCardFrame(
        stats: BufferedImage,
        background: BufferedImage,
        rawAvatar: BufferedImage
    ): BufferedImage {
        val image = rescaleOp.filter(background, null)
        val graphics = image.createGraphics()

        graphics.drawImage(stats, 0, 0, CARD_WIDTH, CARD_HEIGHT, null)
        graphics.drawAvatar(rawAvatar)

        graphics.dispose()
        return image
    }

    private fun Graphics2D.drawAvatar(rawAvatar: BufferedImage) {
        val avatar = BufferedImage(RAW_AVATAR_SIZE, RAW_AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB)
        val avatarGraphics = avatar.createGraphics()
        avatarGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        avatarGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        avatarGraphics.setRenderingHint(
            RenderingHints.KEY_ALPHA_INTERPOLATION,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
        )

        val rawAvatarSize = RAW_AVATAR_SIZE - RAW_AVATAR_BORDER_SIZE * 2
        avatarGraphics.clip =
            Ellipse2D.Float(
                RAW_AVATAR_BORDER_SIZE.toFloat(),
                RAW_AVATAR_BORDER_SIZE.toFloat(),
                rawAvatarSize.toFloat(), rawAvatarSize.toFloat()
            )
        avatarGraphics.drawImage(
            rawAvatar,
            RAW_AVATAR_BORDER_SIZE,
            RAW_AVATAR_BORDER_SIZE,
            rawAvatarSize,
            rawAvatarSize,
            null
        )
        avatarGraphics.dispose()

        val downscaledAvatar = BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB)
        val downscaledAvatarG = downscaledAvatar.createGraphics()
        downscaledAvatarG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        downscaledAvatarG.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        )
        downscaledAvatarG.setRenderingHint(
            RenderingHints.KEY_ALPHA_INTERPOLATION,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
        )
        downscaledAvatarG.drawImage(avatar, 0, 0, AVATAR_SIZE, AVATAR_SIZE, null)
        downscaledAvatarG.dispose()

        drawImage(downscaledAvatar, BORDER_SIZE, BORDER_SIZE, AVATAR_SIZE, AVATAR_SIZE, null)
    }

    private suspend fun getAvatar(user: JDAUser): ImageInfo {
        avatarCache.getIfPresent(user.idLong)?.let { return it }
        val inputStream = user.effectiveAvatar.download(RAW_AVATAR_SIZE).await()
        return extractFramesFromGif(inputStream).also {
            avatarCache.put(user.idLong, it)
        }
    }

    private fun extractFramesFromGif(inputStream: InputStream): ImageInfo {
        val decoder = GifDecoder()
        decoder.read(inputStream)

        val frameCount = decoder.frameCount

        val frames = buildList {
            for (i in 0 until frameCount) {
                val frame = decoder.getFrame(i).toBGR()
                val delay = decoder.getDelay(i)

                val gifFrame = GifFrame(frame, delay)
                add(gifFrame)
            }
        }

        return ImageInfo(frames, true)
    }

    private fun getDefaultBackground(): ImageInfo = ResourceService.getResourceFile("rankcard/default.png") {
        val file = ResourceService.getFile(it).readBytes().toBufferedImage().toBGR()

        ImageInfo(
            listOf(GifFrame(file, 0)),
            isAnimated = false
        )
    }

    private fun getBackground(user: User): ImageInfo {
        val userId = user.id.value
        backgroundCache.getIfPresent(userId)?.let { return it }

        val fileName = user.rankBackground?.background
            ?: return defaultBackground

        val backgroundPath = backgroundDir.resolve(fileName)
        if (!backgroundPath.exists())
            return defaultBackground

        val imageInfo = backgroundPath.inputStream().use { inputStream ->
            if (animatedImageExtensions.any { it == backgroundPath.extension }) {
                val frames = extractFramesFromGif(inputStream)
                return@use frames
            }

            val bufferedImage = ImageIO.read(inputStream).toBGR()
            ImageInfo(listOf(GifFrame(bufferedImage, 0)), false)
        }

        backgroundCache.put(userId, imageInfo)
        return imageInfo
    }

    private fun createAnimatedWebp(frames: List<BufferedImage>, frameRate: Double): ByteArray {
        val outputStream = SeekableByteArrayOutputStream()

        FFmpegFrameRecorder(outputStream, CARD_WIDTH, CARD_HEIGHT, 0).use { recorder ->
            recorder.format = "webp"
            recorder.videoCodec = avcodec.AV_CODEC_ID_WEBP
            recorder.frameRate = frameRate
            recorder.isInterleaved = false
            recorder.setOption("loop", "0")
            recorder.start()

            val converter = Java2DFrameConverter()

            frames.forEach {
                val frame = converter.getFrame(it)
                recorder.record(frame)
            }

            recorder.stop()
        }

        return outputStream.toByteArray()
    }
}

fun ByteArray.toBufferedImage(): BufferedImage = ByteArrayInputStream(this).use {
    ImageIO.read(it)
}

fun BufferedImage.toByteArray(format: String = "png"): ByteArray = ByteArrayOutputStream().use {
    ImageIO.write(this, format, it)
    it.toByteArray()
}

fun BufferedImage.toBGR() = convert(BufferedImage.TYPE_3BYTE_BGR)

fun BufferedImage.convert(type: Int): BufferedImage {
    val temp = BufferedImage(width, height, type)
    val graphics = temp.createGraphics()
    graphics.drawImage(this, 0, 0, null)
    graphics.dispose()

    return temp
}