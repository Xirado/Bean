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

package at.xirado.bean.misc;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class Metrics {
    public static final Gauge GUILD_COUNT = Gauge.build()
            .name("bean_guilds")
            .help("Guild Count")
            .register();

    public static final Gauge USER_COUNT = Gauge.build()
            .name("bean_users")
            .help("User Count")
            .register();

    public static final Counter COMMANDS = Counter.build()
            .name("bean_commands_invocations_total")
            .help("Total commands invoked")
            .labelNames("type", "name")
            .register();

    public static final Counter DISCORD_API_REQUESTS = Counter.build()
            .name("bean_discord_requests")
            .help("Total API requests to Discord")
            .labelNames("response_code")
            .register();

    public static final Counter REQUESTS = Counter.build()
            .name("bean_requests_total")
            .help("Requests")
            .labelNames("type")
            .register();

    public static final Counter MESSAGES = Counter.build()
            .name("bean_received_messages_total")
            .help("Total Messages Received")
            .labelNames("type")
            .register();

    public static final Counter EVENTS = Counter.build()
            .name("bean_received_events_total")
            .help("Total Events Received")
            .register();

    public static final Gauge BUSY_THREADS = Gauge.build()
            .name("bean_busy_threads")
            .help("Busy threads")
            .labelNames("type")
            .register();

    public static final Gauge DISCORD_REST_PING = Gauge.build()
            .name("bean_rest_ping")
            .help("Discord REST Ping")
            .register();

    public static final Gauge DISCORD_GATEWAY_PING = Gauge.build()
            .name("bean_ws_ping")
            .help("Discord Gateway Ping")
            .labelNames("shard_id")
            .register();

}
