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

package at.xirado.bean.log

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

const val DEFAULT_WEBHOOK_PATTERN: String = "%highlight(%-5level) %msg"

fun initWebhookLogger(level: String = "info", webhookUrl: String, pattern: String = DEFAULT_WEBHOOK_PATTERN, waitTime: Long = 5000) {
    val lc = LoggerFactory.getILoggerFactory() as LoggerContext
    val encoder = PatternLayoutEncoder().apply {
        this.context = lc
        this.pattern = pattern
        start()
    }

    WebhookAppender(level, waitTime, webhookUrl, encoder).apply {
        context = lc
        start()

        val logger = LoggerFactory.getLogger("ROOT") as Logger

        logger.addAppender(this)
    }
}

open class WebhookAppender(
    val level: String,
    val waitTime: Long,
    private val webhookUrl: String,
    private val encoder: PatternLayoutEncoder,
) : AppenderBase<ILoggingEvent>() {
    companion object {
        private val guard = ThreadLocal.withInitial { false }
    }

    var timeout: String = "30000"

    private lateinit var client: WebhookClient
    private lateinit var pool: ScheduledExecutorService

    private val buffer = StringBuilder(2000)

    private fun flush() = synchronized(buffer) {
        if (buffer.isEmpty()) return@synchronized
        val message = "```ansi\n${buffer}```"
        buffer.setLength(0)
        client.send(message).exceptionally { null }
    }

    override fun append(event: ILoggingEvent) {
        if (guard.get()) return
        val msg = encoder.encode(event).toString(Charsets.UTF_8)
        synchronized(buffer) {
            msg.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                if (buffer.length + line.length > 1900)
                    flush()
                buffer.append(line).append("\n")
            }
        }
    }

    override fun start() {
        // Set level threshold to prevent spam
        addFilter(ThresholdFilter().apply {
            setLevel(level)
            start()
        })

        pool = Executors.newSingleThreadScheduledExecutor {
            thread(start = false, isDaemon = true, name = "WebhookAppender") {
                guard.set(true)
                it.run()
            }
        }

        client = WebhookClientBuilder(webhookUrl)
            .setWait(false)
            .setExecutorService(pool)
            .build()
            .setTimeout(timeout.toLong())

        pool.scheduleAtFixedRate(this::flush, waitTime, waitTime, TimeUnit.MILLISECONDS)
        encoder.start()
        super.start()
    }

    override fun stop() {
        if (::pool.isInitialized)
            pool.shutdown()
        super.stop()
    }
}