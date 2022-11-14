import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Script to compare two issue sets -- nonnull and nullable.
 * @author jens dietrich
 */
public class Comparison {

    public static void main (String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("Three arguments expected -- files containing nonnull and nullable issues (specs), and the project root folder");
        }

        File nonnullInput = new File(args[0]);
        File nullableInput = new File(args[1]);
        File projectRootFolder = new File(args[2]);

        Preconditions.checkState(projectRootFolder.exists());

        System.out.println("Reading nonnull issues from " + nonnullInput.getAbsolutePath());
        Set<String> jast = read(nonnullInput);


        System.out.println("Reading nullable issues from " + nullableInput.getAbsolutePath());
        Set<String> nai = read(nullableInput);

        System.out.println("\tJASTADDJ: " + jast.size());
        System.out.println("\tNAI: " + nai.size());

        System.out.println("\tINTERSECT(JASTADDJ,NAI): " + Sets.intersection(jast,nai).size());

        Map<CountAnnotatables.KEY,Integer> counters = CountAnnotatables.findAnnotatables(projectRootFolder);
        int max = counters.get(CountAnnotatables.KEY.field) + counters.get(CountAnnotatables.KEY.method_returns) + counters.get(CountAnnotatables.KEY.method_args);

        System.out.println("\tANNOTATABLE: " + max);

    }



    static Set<String> read(File json) {
        Set<String> encodedIssues = new HashSet<>();
        try (FileInputStream in = new FileInputStream(json)) {
            JSONTokener tokener = new JSONTokener(in);
            JSONArray jarr = new JSONArray(tokener);
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject jobj = jarr.getJSONObject(i);
                String s = "" + jobj.getString("className")
                        + "::"
                        + jobj.getString("descriptor")
                        + "@"
                        + jobj.getInt("argsIndex");
                encodedIssues.add(s);
            }
        }
        catch(Exception x){
            System.err.println("Error parsing json: " + json.getAbsolutePath());
            x.printStackTrace();
        }
        return encodedIssues;

    }


}
