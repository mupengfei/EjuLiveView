package view.live.eju.com.ejuliveview;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by ff on 2016/8/17.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
