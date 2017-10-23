package crawler;

import ad.AdResult;
import ad.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Generate the data needed for crawling
 */
public class Scheduler {

    private static final Logger logger = LogManager.getLogger(Scheduler.class);

    private AdResult adResult;

    private String adQueryFilePath;

    private Scheduler() {
    }

    /**
     * @param adResult        The ad result
     * @param adQueryFilePath The ad query file path
     */
    public Scheduler(AdResult adResult, String adQueryFilePath) {
        this();
        this.adResult = adResult;
        this.adQueryFilePath = adQueryFilePath;
    }

    public AdResult getAdResult() {
        return adResult;
    }

    public String getAdQueryFilePath() {
        return adQueryFilePath;
    }

    /**
     * Initialize the query queue for processor's use
     */
    public void Initialize() {
        try (BufferedReader br = new BufferedReader(new FileReader(this.adQueryFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty())
                    continue;

                logger.info(line);
                String[] fields = line.split(Utility.commaSeparator);
                String query = fields[0].trim();
                double bidPrice = Double.parseDouble(fields[1].trim());
                int campaignId = Integer.parseInt(fields[2].trim());
                int queryGroupId = Integer.parseInt(fields[3].trim());

                Query adQuery = new Query();
                adQuery.queryString = query;
                adQuery.bidPrice = bidPrice;
                adQuery.campaignId = campaignId;
                adQuery.queryGroupId = queryGroupId;

                this.adResult.queries.offer(adQuery);
            }
        } catch (IOException e) {
            logger.trace(e.getMessage());
        }
    }
}
