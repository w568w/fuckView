package ml.qingsu.fuckview.utils.online_rules;

import android.content.pm.PackageManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    public static ArrayList<MainActivity.BlockModel> getOnlineRules() throws ExecutionException, InterruptedException {
        FutureTask<ArrayList<MainActivity.BlockModel>> getTask=new FutureTask<>(new Callable<ArrayList<MainActivity.BlockModel>>() {
            @Override
            public ArrayList<MainActivity.BlockModel> call() throws Exception {
                HttpsURLConnection httpsURLConnection= (HttpsURLConnection) new URL("https://w568w.ml/").openConnection();
                InputStream is=httpsURLConnection.getInputStream();
                byte buf[]=new byte[512];
                int size;
                ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                while (is.read(buf)!=-1){
                    byteArrayOutputStream.write(buf);
                }
                String source=byteArrayOutputStream.toString();
                if("".equals(source)||source==null)
                    throw new Exception("Can\'t get list");
               return parse(source.split("\n"));
            }
            private ArrayList<MainActivity.BlockModel> parse(String[] lines){
                ArrayList<MainActivity.BlockModel> list=new ArrayList<>();
                for(String line:lines){
                    if(line.startsWith("#")||line.trim().equals(""))
                        continue;
                    if(MainActivity.ViewModel.isInstance(line))
                        list.add(MainActivity.ViewModel.fromString(line));
                    else
                        list.add(MainActivity.BlockModel.fromString(line));
                }
                return list;
            }
        });
        new Thread(getTask).start();
        return getTask.get();
    }
    public static ArrayList<MainActivity.BlockModel> filterRules(PackageManager packageManager,ArrayList<MainActivity.BlockModel> models){

        for(int len=models.size(),i=0;i<len;i++){
            MainActivity.BlockModel blockModel=models.get(i);
            try {
                packageManager.getApplicationInfo(blockModel.packageName,0);
            } catch (PackageManager.NameNotFoundException e) {
                models.remove(i);
            }
        }
        return models;

    }
}
