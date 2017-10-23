package ad;

import com.google.common.collect.ConcurrentHashMultiset;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AdResult {

    public ConcurrentLinkedQueue<Query> queries;

    public ConcurrentLinkedQueue<Ad> ads;

    public ConcurrentHashMultiset<String> visited;

    public AdResult(){
        this.queries = new ConcurrentLinkedQueue<>();
        this.ads = new ConcurrentLinkedQueue<>();
        this.visited = ConcurrentHashMultiset.create();
    }
}
