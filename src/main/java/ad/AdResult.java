package ad;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;

import java.net.Proxy;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class AdResult {

    public ConcurrentLinkedQueue<Query> queries;

    public ConcurrentLinkedQueue<Ad> ads;

    public ConcurrentHashMultiset<String> queried; // Already queried queries

    public ConcurrentHashMultiset<String> visited; // The crawled url

    public ImmutableList<Proxy> proxies;

    public AtomicInteger queryCount;

    public AdResult(){
        this.queries = new ConcurrentLinkedQueue<>();
        this.ads = new ConcurrentLinkedQueue<>();
        this.queried = ConcurrentHashMultiset.create();
        this.visited = ConcurrentHashMultiset.create();
        this.queryCount = new AtomicInteger(0);
    }

    public void setProxies(List<Proxy> proxies) {
        this.proxies = ImmutableList.copyOf(proxies);
    }
}
