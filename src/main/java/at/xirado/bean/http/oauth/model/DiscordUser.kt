/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.xirado.bean.http.oauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordUser(
    val id: String,
    val username: String,
    val discriminator: String,
    @SerialName("global_name")
    val globalName: String?,
    val avatar: String?,
    val bot: Boolean = false,
    val system: Boolean = false,
    @SerialName("mfa_enabled")
    val mfaEnabled: Boolean = false,
    val banner: String? = null,
    @SerialName("accent_color")
    val accentColor: Int? = null,
    val locale: String? = null,
) {
    @Suppress("UNUSED")
    val avatarUrl = createAvatarUrl()

    private fun createAvatarUrl(): String {
        val avatarHash = avatar
            ?: return if (discriminator == "0")
                "https://cdn.discordapp.com/embed/avatars/${(id.toLong() shr 22) % 6}.png"
            else
                "https://cdn.discordapp.com/embed/avatars/${discriminator.toInt() % 5}.png"

        val isGif = avatarHash.startsWith("a_")
        return "https://cdn.discordapp.com/avatars/$id/$avatarHash.${if (isGif) "gif" else "png"}"
    }
}
