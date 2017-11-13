package crawler;

import ad.AdResult;
import ad.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Generate the data needed for crawling
 */
public class Scheduler {

    private static final Logger logger = LogManager.getLogger(Scheduler.class);

    private AdResult adResult;

    private String adQueryFilePath;

    private String adProxyFilePath;

    private Scheduler() {
        super();
    }

    /**
     * @param adResult        The ad result
     * @param adQueryFilePath The ad query file path
     * @param adProxyFilePath The ad proxy file path
     */
    public Scheduler(AdResult adResult, String adQueryFilePath, String adProxyFilePath) {
        this();
        this.adResult = adResult;
        this.adQueryFilePath = adQueryFilePath;
        this.adProxyFilePath = adProxyFilePath;
    }

    public AdResult getAdResult() {
        return adResult;
    }

    public String getAdQueryFilePath() {
        return adQueryFilePath;
    }

    public String getAdProxyFilePath() {
        return adProxyFilePath;
    }

    /**
     * Initialize both query list and proxy list for crawler
     */
    public void initialize() {
        this.initializeQuery();
        this.initializeProxy();
    }

    /**
     * Initialize the query queue for processor's use
     */
    private void initializeQuery() {
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

            this.adResult.latch = new CountDownLatch(this.adResult.queries.size()); // Set the count to query size
        } catch (IOException e) {
            logger.trace(e.getMessage());
        }
    }

    /**
     * Initialize the proxy list for processor's use
     */
    private void initializeProxy() {
        List<Proxy> proxies = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(this.adProxyFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(Utility.commaSeparator);
                String ip = fields[0].trim();
                int port = Integer.parseInt(fields[1].trim());

                SocketAddress addr = new InetSocketAddress(ip, port);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

                proxies.add(proxy);
            }

            this.adResult.setProxies(proxies);
        } catch (IOException e) {
            logger.trace(e.getMessage());
        }
    }
}
