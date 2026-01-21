package org.example.etl.listener;

import org.example.etl.model.TemperatureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listener that tracks and reports skipped duplicate entries during batch processing.
 */
@Component
public class DuplicateSkipListener implements SkipListener<TemperatureData, TemperatureData> {

    private static final Logger logger = LoggerFactory.getLogger(DuplicateSkipListener.class);

    private final AtomicInteger skipCount = new AtomicInteger(0);

    @Override
    public void onSkipInRead(Throwable t) {
        logger.warn("Skipped item during read: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(TemperatureData item, Throwable t) {
        logger.warn("Skipped item during process: {} - {}", item, t.getMessage());
    }

    @Override
    public void onSkipInWrite(TemperatureData item, Throwable t) {
        skipCount.incrementAndGet();
        logger.info("Duplicate entry skipped: name='{}', datetime='{}', temp={}",
                item.name(), item.datetime(), item.temp());
    }

    /**
     * Returns the total count of skipped duplicates.
     *
     * @return the number of skipped entries
     */
    public int getSkipCount() {
        return skipCount.get();
    }

    /**
     * Resets the skip counter. Typically called before a new job execution.
     */
    public void reset() {
        skipCount.set(0);
    }
}
