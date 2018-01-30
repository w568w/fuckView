package ml.qingsu.fuckview.utils;

import android.content.pm.PackageManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.net.ssl.HttpsURLConnection;

import ml.qingsu.fuckview.ui.activities.MainActivity;

/**
 * Created by w568w on 18-1-20.
 */

public class OnlineRulesUtils {
    public static ArrayList<MainActivity.BlockModel> getOnlineRules() throws Exception {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("http://w568w.ml/").openConnection();
        InputStream is = httpsURLConnection.getInputStream();
        byte buf[] = new byte[512];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (is.read(buf) != -1) {
            byteArrayOutputStream.write(buf);
        }
        String source = byteArrayOutputStream.toString();
        if ("".equals(source) || source == null)
            throw new Exception("Can\'t get list");
        return parse(source.split("\n"));


    }

    private static ArrayList<MainActivity.BlockModel> parse(String[] lines) {
        ArrayList<MainActivity.BlockModel> list = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("#") || line.trim().equals(""))
                continue;
            if (MainActivity.ViewModel.isInstance(line))
                list.add(MainActivity.ViewModel.fromString(line));
            else
                list.add(MainActivity.BlockModel.fromString(line));
        }
        return list;
    }


}
