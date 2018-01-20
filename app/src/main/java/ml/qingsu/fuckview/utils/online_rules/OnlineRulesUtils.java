package ml.qingsu.fuckview.utils.online_rules;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.net.ssl.HttpsURLConnection;

import ml.qingsu.fuckview.ui.activities.MainActivity;

/**
 * Created by w568w on 18-1-20.
 */

public class OnlineRulesUtils {
    public static MainActivity.BlockModel[] getOnlineRules() throws ExecutionException, InterruptedException {
        FutureTask<MainActivity.BlockModel[]> getTask=new FutureTask<>(new Callable<MainActivity.BlockModel[]>() {
            @Override
            public MainActivity.BlockModel[] call() throws Exception {
                HttpsURLConnection httpsURLConnection= (HttpsURLConnection) new URL("https://w568w.ml/").openConnection();
                InputStream is=httpsURLConnection.getInputStream();
                byte buf[]=new byte[512];
                int size;
                ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                while (is.read(buf)!=-1){
                    byteArrayOutputStream.write(buf);
                }
                String source=byteArrayOutputStream.toString();
                return new MainActivity.BlockModel[0];
            }
        });
        new Thread(getTask).start();
        return getTask.get();
    }
}
