package crawler;

import ad.AdResult;

public class CrawlerMain {

    public static void main(String[] args){
        if (args.length < 2) {
            System.out.println("Usage: Crawler <rawQueryDataFilePath> <adsDataFilePath> <proxyFilePath> <crawlerCount>");
            System.exit(0);
        }

        String rawQueryDataFilePath = args[0];
        String adsDataFilePath = args[1];
        String proxyFilePath = args[2];
        int crawlerCount = Integer.parseInt(args[3]);

        AdResult adResult = new AdResult();
        Scheduler scheduler = new Scheduler(adResult, rawQueryDataFilePath, proxyFilePath);
        scheduler.initialize();

        Pipeline pipeline = new Pipeline(adResult, adsDataFilePath);

        PageProcessor[] processors = new PageProcessor[crawlerCount];
        for (int i = 0; i < crawlerCount; i++) {
            PageProcessor processor = new PageProcessor(adResult, i + 1); // starts from 1
            processors[i] = processor;
        }

        // Start crawling
        for (PageProcessor pr : processors) {
            pr.run();
        }

        pipeline.run();
    }
}
