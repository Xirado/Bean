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
            .labelNames("type")
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

    public static final Gauge PLAYING_MUSIC_PLAYERS = Gauge.build()
            .name("bean_playing_music_players")
            .help("Playing music players")
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
