package at.xirado.bean.data.user

import at.xirado.simplejson.JSONObject
import at.xirado.simplejson.SerializableData
import at.xirado.simplejson.get
import java.nio.file.Files
import kotlin.io.path.Path

data class RankCardConfig(var background: String = "default",
                          var accentColor: Int = 0x0C71E0) : SerializableData {

    override fun toData() = JSONObject.empty()
        .put("background", background)
        .put("accent", accentColor)

    @Synchronized
    fun deleteBackground() {
        if (background == "default")
            return
        val path = Path("backgrounds", background)
        if (path.toFile().exists())
            runCatching { Files.delete(path) }
    }

    companion object {
        fun fromData(json: JSONObject) = RankCardConfig(
            json.get<String>("background"),
            json.get<Int>("accent")
        )
    }
}