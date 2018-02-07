package ml.qingsu.fuckview.utils;

import ml.qingsu.fuckview.models.BlockModel;
import ml.qingsu.fuckview.models.ViewModel;
import ml.qingsu.fuckview.ui.activities.MainActivity;

/**
 * Created by w568w on 18-1-19.
 */

public final class ConvertUtils {
    public static BlockModel oldToNew(BlockModel model){
        String oldRecord=model.record;
        if(oldRecord.contains(MainActivity.ALL_SPLIT)){
            return model;
        }
        if(oldRecord.endsWith("#")){
            return new ViewModel(model.packageName," "+MainActivity.ALL_SPLIT+model.record+MainActivity.ALL_SPLIT+" ",model.text,model.className);
        }else if(oldRecord.endsWith("$$")){
            return new ViewModel(model.packageName," "+MainActivity.ALL_SPLIT+" "+MainActivity.ALL_SPLIT+model.record,model.text,model.className);

        }else{
            try{
                //test
                Long.parseLong(oldRecord);
                return new ViewModel(model.packageName,model.record+MainActivity.ALL_SPLIT+" "+MainActivity.ALL_SPLIT+" ",model.text,model.className);
            }catch (NumberFormatException failed){
                return model;
            }
        }
    }
}
