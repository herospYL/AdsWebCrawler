package crawler;

import ad.Ad;
import ad.AdResult;
import ad.Query;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * Crawl Amazon page and store in memory
 */
public class PageProcessor extends Thread{

    private static final Logger logger = LogManager.getLogger(Scheduler.class);

    private static final Marker PROXY_MARKER = MarkerManager.getMarker("Proxy");

    private static final Marker HTML_MARKER = MarkerManager.getMarker("HTML");

    private static final String AMAZON_QUERY_URL = "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=";

    private static final String PROXY_TEST_URL = "http://www.toolsvoid.com/what-is-my-ip-address";

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";

    private static final String titleSelector = " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2";

    private static final String categorySelector = "#leftNav > #leftNavContainer > a-unordered-list.a-nostyle.a-vertical.a-spacing-base > div.a-row.a-expander-container.a-expander-extend-container > li:nth-child(1) > span > a > h4";

    private static final String thumbnailSelector = " > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img";

    private static final String detailSelector = " > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a";

    private static final String brandSelector = " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(2) > span:nth-child(2)";

    private static final String priceSelector = " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-last-child(1) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span.a-color-base sx-zero-spacing > span > span";

    private static final String priceFractionSelector = " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-last-child(1) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span.a-color-base sx-zero-spacing > span > sup.sx-price-fractional";

    private static ImmutableMap<String, String> headers;

    private AdResult adResult;

    private int number;

    public int timeOut = 10000;

    public int totalPages = 5;

    static {
        HashMap<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        defaultHeaders.put("Accept-Encoding", "gzip, deflate, sdch, br");
        defaultHeaders.put("Accept-Language", "en-US,en;q=0.8");
        headers = ImmutableMap.copyOf(defaultHeaders);
    }

    private PageProcessor(){
        super();
    }

    /**
     * @param adResult The ad result instance
     */
    public PageProcessor(AdResult adResult, int number) {
        super(String.valueOf(number));
        this.adResult = adResult;
        this.number = number;
    }

    @Override
    public void run() {
        while (true) {
            int queryCount = this.adResult.queryCount.get();
            if (queryCount <= 0) {
                break;
            }

            Proxy proxy = this.adResult.proxies.get(queryCount % this.number);

            if (!this.adResult.queries.isEmpty()) {
                Query query = this.adResult.queries.poll();
                this.process(query, proxy);
                this.adResult.queryCount.decrementAndGet(); // The query count is a flag to quit program, other than that, no block lock needed, use Concurrent package instead
            }

            // Otherwise, wait for query count's decrement
        }
    }

    private boolean testProxy(Proxy proxy) {
        try {
            Document doc = Jsoup.connect(PROXY_TEST_URL).proxy(proxy).userAgent(USER_AGENT).timeout(this.timeOut).get();
            String ip = doc.select("body > section.articles-section > div > div > div > div.col-md-8.display-flex > div > div.table-responsive > table > tbody > tr:nth-child(1) > td:nth-child(2) > strong").first().text(); //get used IP.
            logger.trace(PROXY_MARKER, this.getThreadName() + "Proxy is valid, IP: " + ip);
            return true;
        } catch (IOException e) {
            logger.warn(PROXY_MARKER, this.getThreadName() + "Proxy is invalid");
            return false;
        }
    }

    private String getThreadName(){
        return "Thread: " + currentThread().getName();
    }

    /**
     * Process an ad query
     * @param query The query object
     * @param proxy The proxy object
     */
    private void process(Query query, Proxy proxy) {
        try {
            testProxy(proxy);

            List<String> queryStrTokenList = Utility.cleanedTokenize(query.queryString);
            Set<String> nGramStrSet = Utility.nGram(queryStrTokenList);

            for (String queryStr : nGramStrSet) {
                for (int page = 1; page <= this.totalPages; page++) {
                    String url = AMAZON_QUERY_URL + queryStr + "&page=" + page;

                    this.processSingle(url, query, proxy); // nGram's query results are all counted on the original one
                    Thread.sleep(0);
                }
            }
        }
        catch (NumberFormatException | IOException | InterruptedException e) {
            logger.trace(e.getMessage());
        }
    }

    private void processSingle(String url, Query query, Proxy proxy) throws NumberFormatException, IOException {
        url = url.replace(" ", "%20");

        Document doc = Jsoup.connect(url).proxy(proxy).headers(headers).userAgent(USER_AGENT).timeout(this.timeOut).get();

        //System.out.println(doc.text());
        //#s-results-list-atf
        Elements results = doc.select("#s-results-list-atf").select("li"); // This is every result block's parent element
        logger.info(HTML_MARKER, "Numbers of results: " + results.size());

        for (int i = 0; i < results.size(); i++) {
            Ad ad = new Ad();
            ad.query = query.queryString;
            ad.queryGroupId = query.queryGroupId;
            ad.keyWords = new ArrayList<>();
            ad.bidPrice = query.bidPrice;
            ad.campaignId = query.campaignId;
            ad.price = 0.0;

            // Get title
            String title_ele_path = "#result_" + Integer.toString(i) + titleSelector;
            Element title_ele = doc.select(title_ele_path).first();
            if (title_ele != null) {
                logger.info(HTML_MARKER, query + " No. " + i + " title: " + title_ele.text());
                ad.title = title_ele.text();
            }

            if (Strings.isNullOrEmpty(ad.title)) {
                logger.warn(HTML_MARKER, "Cannot parse title for " + query);
                continue;
            }

            // Title clean
            List<String> cleanedTitle = Utility.cleanedTokenize(ad.title);
            ad.title = Joiner.on(Utility.spaceSeparator).skipNulls().join(cleanedTitle);

            // Get detail, do dedupe
            String detail_path = "#result_" + Integer.toString(i) + detailSelector;
            Element detail_url_ele = doc.select(detail_path).first();
            if (detail_url_ele != null) {
                logger.info(HTML_MARKER, query + " No. " + i + " detail: " + detail_url_ele.attr("href"));
                ad.detailUrl = detail_url_ele.attr("href");
            } else {
                logger.warn(HTML_MARKER,"Cannot parse detail for query:" + query + ", title: " + ad.title);
                continue;
            }

            // Dedupe, do collection update at the end
            if (this.adResult.queried.contains(ad.detailUrl)) {
                continue; // skip
            }

            // Get thumbnail
            String thumbnail_path = "#result_" + Integer.toString(i) + thumbnailSelector;
            Element thumbnail_ele = doc.select(thumbnail_path).first();
            if (thumbnail_ele != null) {
                logger.info(HTML_MARKER, query + " No. " + i + " thumbnail: " + thumbnail_ele.attr("src"));
                ad.thumbnail = thumbnail_ele.attr("src");
            } else {
                logger.warn(HTML_MARKER,"Cannot parse thumbnail for query:" + query + ", title: " + ad.title);
                continue;
            }

            // Get brand
            String brand_path = "#result_" + Integer.toString(i) + brandSelector;
            Element brand = doc.select(brand_path).first();
            if (brand != null) {
                logger.info(HTML_MARKER, query + " No. " + i + " brand: " + brand.text());
                ad.brand = brand.text();
            } else {
                logger.warn(HTML_MARKER,"Cannot parse brand for query:" + query + ", title: " + ad.title);
                continue;
            }

            // Get prices
            String price_whole_path = "#result_" + Integer.toString(i) + priceSelector;
            String price_fraction_path = "#result_" + Integer.toString(i) + priceFractionSelector;
            Element price_whole_ele = doc.select(price_whole_path).first();
            if (price_whole_ele != null) {
                String price_whole = price_whole_ele.text();
                logger.info(HTML_MARKER, query + " No. " + i + " price whole: " + price_whole);

                // remove ","
                // E.g.: 1,000
                if (price_whole.contains(Utility.commaSeparator)) {
                    price_whole = price_whole.replaceAll(Utility.commaSeparator, "");
                }
                ad.price = Double.parseDouble(price_whole);
            } else {
                logger.warn(HTML_MARKER,"Cannot parse price whole for query:" + query + ", title: " + ad.title);
                continue;
            }

            Element price_fraction_ele = doc.select(price_fraction_path).first();
            if (price_fraction_ele != null) {
                logger.info(HTML_MARKER, query + " No. " + i + " price fraction: " + price_fraction_ele.text());
                ad.price = ad.price + Double.parseDouble(price_fraction_ele.text()) / 100.0;
            } else {
                logger.warn(HTML_MARKER,"Cannot parse price fraction for query:" + query + ", title: " + ad.title);
                continue;
            }

            // Get category
            Element category_ele = doc.select(categorySelector).first();
            if (category_ele != null) {
                logger.info(HTML_MARKER, query + " No. " + i + " category: " + category_ele.text());
                ad.category = category_ele.text();
            }

            if (Strings.isNullOrEmpty(ad.category)) {
                logger.warn(HTML_MARKER,"Cannot parse category for query:" + query + ", title: " + ad.title);
                continue;
            }

            this.adResult.queried.add(ad.query);
            this.adResult.visited.add(ad.detailUrl);
            this.adResult.ads.offer(ad);
        }
    }
}
