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

package at.xirado.bean.ktx

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.SubscribeEvent
import kotlin.coroutines.resume
import kotlin.time.Duration

suspend inline fun <reified T : GenericEvent> JDA.await(
    timeout: Duration,
    crossinline filter: (T) -> Boolean = { true }
): T? {
    return withTimeoutOrNull(timeout) {
        return@withTimeoutOrNull suspendCancellableCoroutine<T> {
            val listener = object : EventListener {
                @SubscribeEvent
                override fun onEvent(event: GenericEvent) {
                    if (event is T && filter(event)) {
                        removeEventListener(this)
                        it.resume(event)
                    }
                }
            }
            addEventListener(listener)
            it.invokeOnCancellation { removeEventListener(listener) }
        }
    }
}
