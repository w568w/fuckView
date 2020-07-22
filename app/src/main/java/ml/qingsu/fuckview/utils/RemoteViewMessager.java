package ml.qingsu.fuckview.utils;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import ml.qingsu.fuckview.Constant;
import ml.qingsu.fuckview.IViewMessager;
import ml.qingsu.fuckview.binder.BitmapBinder;
import ml.qingsu.fuckview.models.ViewCaptureEvent;

public class RemoteViewMessager extends Service {
    private static final String BROADCAST_ACTION = "tooYoungtooSimple";

    private final IViewMessager.Stub stub = new IViewMessager.Stub() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public Bundle shareMemory(Bundle bundle) throws RemoteException {
            ParcelFileDescriptor parcelable = bundle.getParcelable("client");
            FileDescriptor fd = parcelable.getFileDescriptor();
            FileInputStream fileInputStream = new FileInputStream(fd);
            Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
            EventBus.getDefault().post(new ViewCaptureEvent(bundle, bitmap));
            return null;
        }
    };

    private static byte[] Bitmap2Bytes(Bitmap bm) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);

        return baos.toByteArray();

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static Bundle addBitmapToBundle(Bundle bundle, Bitmap bitmap) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MemoryFile file = new MemoryFile(UUID.randomUUID().toString(), bitmap.getByteCount());
        file.getOutputStream().write(Bitmap2Bytes(bitmap));
        //获取文件FD
        Method method = MemoryFile.class.getDeclaredMethod("getFileDescriptor");
        method.setAccessible(true);
        FileDescriptor fd = (FileDescriptor) method.invoke(file);
//保存FD到这个序列化对象
        ParcelFileDescriptor descriptor = ParcelFileDescriptor.dup(fd);
//创建Bundle，传递对象
        bundle.putParcelable("client", descriptor);
        return bundle;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }
}
