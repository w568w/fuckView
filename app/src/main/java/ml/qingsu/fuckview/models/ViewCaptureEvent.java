package ml.qingsu.fuckview.models;

import android.graphics.Bitmap;
import android.os.Bundle;

public class ViewCaptureEvent {
    public Bundle bundle;
    public Bitmap bitmap;

    public ViewCaptureEvent(Bundle bundle, Bitmap bitmap) {
        this.bundle = bundle;
        this.bitmap = bitmap;
    }
}
