package crawler;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The utility methods for query understanding
 */
public class Utility {
    /**
     * The common stop words
     */
    private static final String stopWords = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";

    public static final String spaceSeparator = " ";

    public static final String commaSeparator = ",";

    /**
     * @param stopWords Simple stop words string
     * @return Lucene CharArraySet
     */
    private static CharArraySet getStopWords(String stopWords) {
        List<String> stopWordsList = new ArrayList<>();
        for (String stop : stopWords.split(commaSeparator)) {
            stopWordsList.add(stop.trim());
        }
        return new CharArraySet(stopWordsList, true);
    }

    /**
     * @param tokens A list of token strings
     * @return A set of n-gram
     */
    public static Set<String> nGram(List<String> tokens) {
        Set<String> set = new HashSet<>();

        int len = tokens.size();
        for (int window = 2; window <= len; window++) {
            for (int start = 0; start + window <= len; start++) {
                StringBuilder sb = new StringBuilder();
                for (int i = start; i < start + window; i++) {
                    if (i > start) {
                        sb.append(spaceSeparator);
                    }

                    sb.append(tokens.get(i));
                }

                set.add(sb.toString());
            }
        }

        return set;
    }

    /**
     * @param input The raw input string
     * @return The clean output string after removing stop words, tokenize and stem
     * @throws IOException Tokenizer IOException
     */
    public static List<String> cleanedTokenize(String input) throws IOException {
        List<String> tokens = new ArrayList<>();
        StringReader reader = new StringReader(input.toLowerCase());
        Tokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(reader);
        TokenStream tokenStream = new StandardFilter(tokenizer);
        tokenStream = new StopFilter(tokenStream, getStopWords(stopWords));
        tokenStream = new KStemFilter(tokenStream);
        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            String term = charTermAttribute.toString();

            tokens.add(term);
        }
        tokenStream.end();
        tokenStream.close();

        tokenizer.close();

        return tokens;
    }
}