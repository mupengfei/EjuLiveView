package view.live.eju.com.ejuliveview;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import view.live.eju.ejudanmuku.EjuDanmukuView;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private View hello;
    private View test;
    private View cancel;
    private View resume;
    private View send;
    private EditText text;
    private int windowWidth;
    private ObjectAnimator animator;
    private EjuDanmukuView danmukuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        setContentView(R.layout.activity_main);
        hello = this.findViewById(R.id.hello);
        test = this.findViewById(R.id.test);
        cancel = this.findViewById(R.id.cancel);
        resume = this.findViewById(R.id.resume);
        send = this.findViewById(R.id.send);
        text = (EditText) this.findViewById(R.id.text);
        danmukuView = (EjuDanmukuView) this.findViewById(R.id.danmuku_view);
        danmukuView.setDanmukuOnClickListener(new EjuDanmukuView.DanmukuOnClickListener() {
            @Override
            public void onClick(EjuDanmukuView.DanmukuObject obj) {
                Log.e("TAG", obj.getContent());
            }
        });
        WindowManager wm = this.getWindowManager();
        windowWidth = wm.getDefaultDisplay().getWidth();

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                danmukuView.resumeDanmuku();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                danmukuView.send("1", R.drawable.ic_launcher, "用户名45:", text.getText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        danmukuView.destroy();
    }
}
