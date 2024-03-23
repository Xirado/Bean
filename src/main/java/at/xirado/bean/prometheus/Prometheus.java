package at.xirado.bean.prometheus;

import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Prometheus {
    private static final Logger LOG = LoggerFactory.getLogger(Prometheus.class);

    public Prometheus() {
        DefaultExports.initialize();
        try {
            new HTTPServer(5123);
        } catch (IOException ex) {
            LOG.error("Could not initialize Prometheus HTTP Server!", ex);
        }
    }
}
