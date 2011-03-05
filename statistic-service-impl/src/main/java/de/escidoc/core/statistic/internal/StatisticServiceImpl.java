package de.escidoc.core.statistic.internal;

import de.escidoc.core.sm.business.interfaces.StatisticDataHandlerInterface;
import de.escidoc.core.statistic.StatisticServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of {@link de.escidoc.core.statistic.StatisticService}.
 *
 * @author <a href="mailto:mail@eduard-hildebrandt.de">Eduard Hildebrandt</a>
 */
public class StatisticServiceImpl {

    private static final Log LOG = LogFactory.getLog(StatisticServiceImpl.class);

    private StatisticDataHandlerInterface statisticDataHandler;

    /**
     * Saves the static data string in the repository.
     *
     * @param statisticData the statistic data XML string
     * @throws StatisticServiceException if any error accours.
     */
    public void createStatisticRecord(final String statisticData) throws StatisticServiceException {
        try {
            // TODO: Refactor StatisticDataHandler and move to this module.
            this.statisticDataHandler.insertStatisticData(statisticData);
        } catch (final Exception e) {
            final String errorMessage = "Error on saving statistic data.";
            LOG.error(errorMessage, e);
            throw new StatisticServiceException(statisticData + '\n' + e);
        }
    }

    /**
     * Sets the StatisticDataHandler.
     *
     * @param statisticDataHandler the StatisticDataHandlerInterface.
     */
    public void setStatisticDataHandler(final StatisticDataHandlerInterface statisticDataHandler) {
        this.statisticDataHandler = statisticDataHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "StatisticServiceImpl{" +
                "statisticDataHandler=" + statisticDataHandler +
                '}';
    }
}
