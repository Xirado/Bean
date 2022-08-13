package at.xirado.bean.data.guild

import at.xirado.simplejson.*

data class LevelingConfig(var minExperience: Int = 15,
                          var maxExperience: Int = 25,
                          var blacklistedChannels: MutableSet<Long> = mutableSetOf()) : SerializableData {

    override fun toData() = JSONObject.empty()
        .put("experience_min", minExperience)
        .put("experience_max", maxExperience)
        .put("blacklisted_channels", JSONArray.fromCollection(blacklistedChannels))

    companion object {
        fun fromData(json: JSONObject): LevelingConfig {
            return LevelingConfig(
                json.get<Int>("experience_min"),
                json.get<Int>("experience_max"),
                json.get<JSONArray>("blacklisted_channels").asSequence<Long>().toMutableSet()
            )
        }
    }
}
