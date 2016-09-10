package view.live.eju.ejudanmuku;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by ff on 2016/8/10.
 */
public class EjuDanmukuView extends FrameLayout {
    private final int _DANMUKU_LINE_FIRST_ = 0;
    private final int _DANMUKU_LINE_SECOND_ = 1;
    final String TAG = "EjuDanmukuView";
    private LinkedBlockingQueue<DanmukuObject> mQueue = new LinkedBlockingQueue<DanmukuObject>();
    private Thread mDealThread = null;
    private ViewGroup mFirstLine;
    private ViewGroup mSecondLine;
    private int mWindowWidth;
    private Handler mHandler = new Handler(getContext().getMainLooper());
    private boolean mFlagFirst = false;
    private boolean mFlagSecond = false;

    private DanmukuOnClickListener mListener;

    public EjuDanmukuView(Context context) {
        super(context);
        init(context);
    }

    public EjuDanmukuView(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_eju_danmuku, this);
        mFirstLine = (ViewGroup) findViewById(R.id.danmuku_first_line);
        mSecondLine = (ViewGroup) findViewById(R.id.danmuku_second_line);
        WindowManager wm = ((Activity) context).getWindowManager();
        mWindowWidth = wm.getDefaultDisplay().getWidth();
        if (mDealThread == null)
            mDealThread = new Thread(new DealDanmuku());
        mDealThread.start();
    }

    public void destroy() {
        mDealThread.interrupt();
        mDealThread = null;
        mQueue.clear();
    }

    private void clickDanmuku(DanmukuObject obj) {
//        pause(num);
        if (mListener != null)
            mListener.onClick(obj);
    }

    public void setDanmukuOnClickListener(DanmukuOnClickListener listener) {
        this.mListener = listener;
    }

    private void send(int line, final DanmukuObject obj) {
        if (line == _DANMUKU_LINE_FIRST_) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final View danmukuViewObj = LayoutInflater.from(getContext()).inflate(R.layout.view_eju_danmuku_obj, null);
                    goDanmuku(danmukuViewObj, _DANMUKU_LINE_FIRST_, obj);
                }
            });
        } else if (line == _DANMUKU_LINE_SECOND_) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final View danmukuViewObj = LayoutInflater.from(getContext()).inflate(R.layout.view_eju_danmuku_obj, null);
                    goDanmuku(danmukuViewObj, _DANMUKU_LINE_SECOND_, obj);
                }
            });
        }
    }

    public void send(String uid, int img, String user, String content) {
        try {
            this.mQueue.add(new DanmukuObject(uid, img, user, content));
        } catch (Exception e) {
        }
    }

    private class DealDanmuku implements Runnable {
        public void run() {
            while (mDealThread != null && !mDealThread.isInterrupted()) {
                try {
                    if (!mFlagFirst) {
                        DanmukuObject obj = mQueue.take();
                        mFlagFirst = true;
                        send(_DANMUKU_LINE_FIRST_, obj);
                        continue;
                    } else if (!mFlagSecond) {
                        DanmukuObject obj = mQueue.take();
                        mFlagSecond = true;
                        send(_DANMUKU_LINE_SECOND_, obj);
                        continue;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    public class DanmukuObject {
        private String uid;
        private int img;
        private String user;
        private String content;

        DanmukuObject(String uid, int img, String user, String content) {
            this.uid = uid;
            this.img = img;
            this.user = user;
            this.content = content;
        }

        public String getUid() {
            return uid;
        }

        public int getImg() {
            return img;
        }

        public String getUser() {
            return user;
        }

        public String getContent() {
            return content;
        }
    }

    public interface DanmukuOnClickListener {
        void onClick(DanmukuObject obj);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void goDanmuku(final View view, final int line, final DanmukuObject obj) {
        if (line == _DANMUKU_LINE_FIRST_) {
            mFirstLine.addView(view);
        } else if (line == _DANMUKU_LINE_SECOND_)
            mSecondLine.addView(view);
        else
            return;
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clickDanmuku(obj);
            }
        });
        ((ImageView) view.findViewById(R.id.danmuku_img)).setImageResource(obj.getImg());
        ((TextView) view.findViewById(R.id.danmuku_user)).setText(obj.getUser());
        ((TextView) view.findViewById(R.id.danmuku_content)).setText(obj.getContent() + " ");
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "x", mWindowWidth, 0);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                finishDanmuku(view, line, obj);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.setDuration((obj.content.length() + obj.user.length()) > 8 ? 10000 : 7000).start();
    }

    private void finishDanmuku(final View view, final int line, final DanmukuObject obj) {
        ObjectAnimator mAnimator = ObjectAnimator.ofFloat(view, "x", view.getX(), view.getMeasuredWidth() * -1);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (line == _DANMUKU_LINE_FIRST_)
                    mFirstLine.removeView(view);
                else if (line == _DANMUKU_LINE_SECOND_)
                    mSecondLine.removeView(view);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        if (mQueue.size() > 0)
            mAnimator.setDuration((obj.content.length() + obj.user.length()) > 8 ? 3000 : 2000).start();
        else
            mAnimator.setDuration(((obj.content.length() + obj.user.length()) > 8 ? 10000 : 7000) * view.getMeasuredWidth() / mWindowWidth).start();
        if (line == _DANMUKU_LINE_FIRST_)
            mFlagFirst = false;
        else if (line == _DANMUKU_LINE_SECOND_)
            mFlagSecond = false;
    }

    private void blur(Bitmap bkg, View view) {
        long startMs = System.currentTimeMillis();
        float scaleFactor = 1;
        float radius = 20;
//        if (downScale.isChecked()) {
//            scaleFactor = 8;
//            radius = 2;
//        }

        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth() / scaleFactor),
                (int) (view.getMeasuredHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);

        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        view.setBackground(new BitmapDrawable(getResources(), overlay));
    }
}
