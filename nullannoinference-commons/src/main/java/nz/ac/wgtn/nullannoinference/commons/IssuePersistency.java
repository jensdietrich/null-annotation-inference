package nz.ac.wgtn.nullannoinference.commons;

import nz.ac.wgtn.nullannoinference.commons.json.JSONArray;
import nz.ac.wgtn.nullannoinference.commons.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * Utility to load / save issues from / to files.
 * As this is also used by agents, it does not use existing data binding.
 * Instead, use shaded low level json library
 * to avoid conflicts with program under analysis.
 * @author jens dietrich
 */
public class IssuePersistency {

    // persistence
    public static void save (Set<Issue> issues, File file) {

        if (issues.isEmpty()) {
            System.out.println("No issues to be saved -- issue set is empty");
            return;
        }

        System.out.println("saving " + issues.size() + " issues to " + file.getAbsolutePath());

        JSONArray array = new JSONArray();
        for (Issue issue:issues) {
            JSONObject jobj = new JSONObject();
            jobj.put("className",issue.getClassName());
            jobj.put("methodName",issue.getMethodName());
            jobj.put("descriptor",issue.getDescriptor());
            jobj.put("argsIndex",issue.getArgsIndex());
            jobj.put("kind",issue.getKind().name());
            jobj.put("index",issue.getArgsIndex());
            jobj.put("context",issue.getContext());
            jobj.put("provenanceType",issue.getProvenanceType().name());
            jobj.put("scope",issue.getScope().name());
            if (issue.getStacktrace()!=null) {
                for (String element : issue.getStacktrace()) {
                    jobj.append("stacktrace",element);
                }
            }
            array.put(jobj);
        }
        System.out.println("null issues serialised: " + array.length());
        String json = array.toString(3);
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println(json);
            System.out.println("null issues observed written to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
