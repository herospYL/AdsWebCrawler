package ad;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;

import java.net.Proxy;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class AdResult {

    public static final Ad POISON_PILL = new Ad(); //special object to kill consumers

    public ConcurrentLinkedQueue<Query> queries;

    public BlockingQueue<Ad> ads;

    public ConcurrentHashMultiset<String> queried; // Already queried queries

    public ConcurrentHashMultiset<String> visited; // The crawled url

    public ImmutableList<Proxy> proxies;

    public CountDownLatch latch;

    public AdResult(){
        this.queries = new ConcurrentLinkedQueue<>();
        this.ads = new LinkedBlockingQueue<>();
        this.queried = ConcurrentHashMultiset.create();
        this.visited = ConcurrentHashMultiset.create();
    }

    public void setProxies(List<Proxy> proxies) {
        this.proxies = ImmutableList.copyOf(proxies);
    }
}
