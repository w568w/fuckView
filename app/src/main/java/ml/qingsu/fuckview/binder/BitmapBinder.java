package ml.qingsu.fuckview.binder;

import android.graphics.Bitmap;
import android.os.Binder;

public class BitmapBinder extends Binder {
    private Bitmap bitmap;

    public BitmapBinder(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void recycle() {
        bitmap.recycle();
    }
}
