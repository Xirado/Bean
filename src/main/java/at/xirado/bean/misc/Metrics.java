package at.xirado.bean.misc;

import io.prometheus.client.Gauge;

public class Metrics
{
    public static final Gauge GUILD_COUNT = Gauge.build()
            .name("bean_guilds")
            .help("Guild Count")
            .register();

    public static final Gauge USER_COUNT = Gauge.build()
            .name("bean_users")
            .help("User Count")
            .register();

    public static final Gauge COMMANDS_PER_MINUTE = Gauge.build()
            .name("bean_commands_per_minute")
            .help("Commands per second")
            .labelNames("type")
            .register();

    public static final Gauge REQUESTS_PER_MINUTE = Gauge.build()
            .name("bean_requests_per_minute")
            .help("Requests per Minute")
            .labelNames("type")
            .register();
}
