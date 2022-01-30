package at.xirado.bean.misc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A counter that counts events within the past time interval. All events that occurred before this interval will be
 * removed from the counter.
 */
public class FrequencyCounter
{

    private final long monitoringInterval;

    private final int[] details;

    private final AtomicInteger currentCount = new AtomicInteger();

    private long startInterval;

    private int total;

    /**
     * Create a new instance of the counter for the given interval.
     *
     * @param interval the time to monitor/count the events.
     * @param unit     the time unit of the {@code interval} argument
     */
    public FrequencyCounter(long interval, TimeUnit unit)
    {
        this(interval, unit, 16);
    }

    /**
     * Create a new instance of the counter for the given interval.
     *
     * @param interval  the time to monitor/count the events.
     * @param unit      the time unit of the {@code interval} argument
     * @param precision the count of time slices for the measurement
     */
    FrequencyCounter(long interval, TimeUnit unit, int precision)
    {
        monitoringInterval = unit.toMillis(interval);
        if (monitoringInterval <= 0)
        {
            throw new IllegalArgumentException("Interval must be a positive value:" + interval);
        }
        details = new int[precision];
        startInterval = System.currentTimeMillis() - monitoringInterval;
    }

    /**
     * Count a single event.
     */
    public void increment()
    {
        checkInterval(System.currentTimeMillis());
        currentCount.incrementAndGet();
    }

    /**
     * Get the current value of the counter.
     *
     * @return the counter value
     */
    public int getCount()
    {
        long currentTime = System.currentTimeMillis();
        checkInterval(currentTime);
        long diff = currentTime - startInterval - monitoringInterval;

        double partFactor = (diff * details.length / (double) monitoringInterval);
        int part = (int) (details[0] * partFactor);
        return total + currentCount.get() - part;
    }

    /**
     * Check the interval of the detail counters and move the interval if needed.
     *
     * @param time the current time
     */
    private void checkInterval(final long time)
    {
        if ((time - startInterval - monitoringInterval) > monitoringInterval / details.length)
        {
            synchronized (details)
            {
                long detailInterval = monitoringInterval / details.length;
                while ((time - startInterval - monitoringInterval) > detailInterval)
                {
                    int currentValue = currentCount.getAndSet(0);
                    if ((total | currentValue) == 0)
                    {
                        // for the case that the counter was not used for a long time
                        startInterval = time - monitoringInterval;
                        return;
                    }
                    int size = details.length - 1;
                    total += currentValue - details[0];
                    System.arraycopy(details, 1, details, 0, size);
                    details[size] = currentValue;
                    startInterval += detailInterval;
                }
            }
        }
    }
}