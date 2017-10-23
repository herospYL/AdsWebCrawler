package ad;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;


/**
 * The Ad result data
 */
public class Ad implements Serializable {

    private static final long serialVersionUID = 1L;

    public int adId;

    public int campaignId;

    public List<String> keyWords;

    public double relevanceScore;

    public double pClick;

    public double bidPrice;

    public double rankScore;

    public double qualityScore;

    public double costPerClick;

    @JsonProperty(required = true)
    public String title;

    @JsonProperty(required = true)
    public double price;

    @JsonProperty(required = true)
    public String thumbnail;

    @JsonProperty(required = true)
    public String description;

    @JsonProperty(required = true)
    public String brand; // TODO: Is it used?

    @JsonProperty(required = true)
    public String detailUrl;

    @JsonProperty(required = true)
    public String query;

    public int queryGroupId;

    public String category;

    //    public int position; // 1: top , 2: bottom
}
