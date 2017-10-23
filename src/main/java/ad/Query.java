package ad;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The query data
 */
public class Query {

    public String queryString;

    public double bidPrice;

    public int campaignId;

    public int queryGroupId;

    /**
     * Indicate if this query has been processed, and if its results have been pushed
     */
    public AtomicBoolean resultsPushed = new AtomicBoolean(false);
}
