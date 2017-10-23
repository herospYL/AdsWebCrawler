package crawler;

import ad.Ad;
import ad.AdResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * The Pipeline of the crawler that does data persistence
 */
public class Pipeline extends Thread {

    private static final Logger logger = LogManager.getLogger(Pipeline.class);

    private AdResult adResult;

    private String adResultFilePath;

    /**
     * The JSON mapper
     */
    private ObjectMapper mapper;

    /**
     * Default constructor
     */
    private Pipeline() {
        this.mapper = new ObjectMapper();
    }

    /**
     * @param adResult         The shared adResult instance
     * @param adResultFilePath The file path for writing down the result
     */
    public Pipeline(AdResult adResult, String adResultFilePath) {
        this();
        this.adResult = adResult;
        this.adResultFilePath = adResultFilePath;
    }

    public AdResult getAdResult() {
        return adResult;
    }

    public String getAdResultFilePath() {
        return adResultFilePath;
    }

    @Override
    public void run() {
        if (adResult != null) {

            File file;
            try {
                file = new File(this.adResultFilePath);
                // if file doesn't exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                logger.trace(e.getMessage());
                return;
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()))) {
                while (true) {
                    if (adResult.ads.isEmpty()) {
                        if (adResult.queries.isEmpty()) {
                            break;
                        } else {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                logger.trace(e.getMessage());
                            }
                        }
                    } else {
                        Ad ad = this.adResult.ads.poll();
                        String jsonInString = this.mapper.writeValueAsString(ad);
                        bw.write(jsonInString);
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                logger.trace(e.getMessage());
                return;
            }

            logger.info("Finish processing results");
        } else {
            logger.debug("The adResult is null");
        }
    }
}
