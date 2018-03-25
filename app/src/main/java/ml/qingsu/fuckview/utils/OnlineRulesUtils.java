package ml.qingsu.fuckview.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ml.qingsu.fuckview.models.BlockModel;
import ml.qingsu.fuckview.models.ViewModel;

/**
 * Created by w568w on 18-1-20.
 */

public class OnlineRulesUtils {
    public static ArrayList<BlockModel> getOnlineRules() throws Exception {
        HttpURLConnection httpsURLConnection = (HttpURLConnection) new URL("http://w568w.ml/rules").openConnection();
        InputStream is = httpsURLConnection.getInputStream();
        byte[] buf = new byte[512];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (is.read(buf) != -1) {
            byteArrayOutputStream.write(buf);
        }
        String source = byteArrayOutputStream.toString();
        if ("".equals(source) || source == null) {
            throw new Exception("Can\'t get list");
        }
        return parse(source.split("\n"));


    }

    private static ArrayList<BlockModel> parse(String[] lines) {
        ArrayList<BlockModel> list = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("#") || "".equals(line.trim())) {
                continue;
            }
            if (ViewModel.isInstance(line)) {
                list.add(ViewModel.fromString(line));
            } else {
                list.add(BlockModel.fromString(line));
            }
        }
        return list;
    }

}
