package com.stalendp.flappybird;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

/**
 * Created by shao on 2016/3/15.
 */
public class MengBanView extends View {

    private int screenWidth;//屏幕宽度
    private int screenHeight;//屏幕高度
    private int screenRadius;//屏幕算出来半径

    private int playRadius;//显示的半径

    private int areaSize = 0;// 空白区域大小
    private int[] position = new int[2];//设置空白区域大小位置

    int backColor = Color.parseColor("#000000");//背景颜色

    public void setShowPosition(int positionX, int positionY) {
        if (positionX <= 0 || positionY <= 0)
            return;
        this.position[0] = positionX;
        this.position[1] = positionY;
        int x = screenWidth, y = screenHeight;
        if (positionX < screenWidth / 2 && positionY < screenHeight / 2) {
            x = screenWidth - positionX;
            y = screenHeight - positionY;
        } else if (positionX >= screenWidth / 2 && positionY < screenHeight / 2) {
            x = positionX;
            y = screenHeight - positionY;
        } else if (positionX < screenWidth / 2 && positionY >= screenHeight / 2) {
            x = screenWidth - positionX;
            y = positionY;
        } else {
            x = positionX;
            y = positionY;
        }
        screenRadius = (int) (Math.sqrt((x * x) + (y * y)));
    }

    public void setAreaSize(int areaSize) {
        if (areaSize < 0)
            return;
        float density = getDensity(getContext());

        this.areaSize = (int) (areaSize * density + 0.5);
    }

    public int getAreaSize(){
        return areaSize;
    }

    //得到设备的密度
    private float getDensity(Context ctx) {
        return ctx.getResources().getDisplayMetrics().density;
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
    }

    public void setPlayRadius(int banjing) {
        this.playRadius = banjing;
        invalidate();
    }

    public int getScreenRadius() {
        return screenRadius;
    }

    public MengBanView(Context context) {
        super(context);
        init();
    }

    public MengBanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MengBanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    Paint paint;

    void init() {
        screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;

        this.position[0] = (screenWidth) / 2;
        this.position[1] = (screenHeight) / 2;
        // 构建Paint时直接加上去锯齿属性
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);//锯齿
        paint.setDither(true);//抖动
        screenRadius = (int) (Math.sqrt((screenHeight * screenHeight) + (screenWidth * screenWidth)) / 2);
        playRadius = screenRadius;
    }

    public void reSet() {
        playRadius = (screenRadius-areaSize);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        paint.setColor(backColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((screenRadius - playRadius - areaSize) * 2);
//        Log.e("mengban-----","屏幕半径："+ screenRadius +"----显示半径："+playRadius);
        canvas.drawCircle(position[0], position[1], screenRadius, paint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.e("mengban","触碰到蒙版");
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    private AnimatorBuilder animatorBuilder;

    /**
     * Animator for the view.
     *
     * @return The AnimatorBuilder to build the animation.
     */
    public AnimatorBuilder getAnimator() {
        if (animatorBuilder == null) {
            animatorBuilder = new AnimatorBuilder(this);
        }
        return animatorBuilder;
    }

    /**
     * Object for building the animation of  this view.
     */
    public static class AnimatorBuilder {
        /**
         * Duration of the animation.
         */
        private int duration = 350;
        /**
         * Interpolator for the time of the animation.
         */
        private Interpolator interpolator;
        /**
         * The delay before the animation.
         */
        private int delay = 0;
        /**
         * ObjectAnimator that constructs the animation.
         */
        private final ObjectAnimator anim;

        private MengBanInterface mengBanInterface;

        public void setMengBanInterface(MengBanInterface mengBanInterface) {
            this.mengBanInterface = mengBanInterface;
        }

        /**
         * Default constructor.
         *
         * @param view The view that must be animated.
         */
        static MengBanView mengBanView;

        private AnimatorBuilder(final View view) {
            anim = ObjectAnimator.ofFloat(view, "percentage", 0.0f, 1.0f);
            if (view instanceof MengBanView) {
                mengBanView = (MengBanView) view;
                getAnim().addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        long currPlayTime = valueAnimator.getCurrentPlayTime();
                        long duration = valueAnimator.getDuration();
                        mengBanView.setPlayRadius((int)((mengBanView.getScreenRadius()-mengBanView.getAreaSize()) *
                                (1 - (currPlayTime * 1.00f) / duration)));

                        if (mengBanInterface != null)
                            mengBanInterface.onAnimationUpdate(valueAnimator);
                    }
                });
                getAnim().addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        if (mengBanInterface != null)
                            mengBanInterface.onAnimationStart(animator);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (mengBanInterface != null)
                            mengBanInterface.onAnimationEnd(animator);
                        else {//没接口默认重制
//                            mengBanView.reSet();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        if (mengBanInterface != null) {
                            mengBanInterface.onAnimationCancel(animator);
                        }
                        else{
                            mengBanView.reSet();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                        mengBanView.reSet();
                        if (mengBanInterface != null)
                            mengBanInterface.onAnimationRepeat(animator);
                    }
                });
            }
        }

        public ObjectAnimator getAnim() {
            return anim;
        }

        /**
         * Set the duration of the animation.
         *
         * @param duration - The duration of the animation.
         * @return AnimatorBuilder.
         */
        public AnimatorBuilder duration(final int duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Set the Interpolator.
         *
         * @param interpolator - Interpolator.
         * @return AnimatorBuilder.
         */
        public AnimatorBuilder interpolator(final Interpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        /**
         * The delay before the animation.
         *
         * @param delay - int the delay
         * @return AnimatorBuilder.
         */
        public AnimatorBuilder delay(final int delay) {
            this.delay = delay;
            return this;
        }
        
        public void reset(){
        	if(mengBanView!=null)
        		mengBanView.reSet();
        }


        /**
         * Starts the animation.
         */
        public void start() {
            anim.setDuration(duration);
            anim.setInterpolator(interpolator);
            anim.setStartDelay(delay);
            anim.start();
        }

        public void setShowPosition(int positionX, int positionY){
            if(mengBanView!=null){
                mengBanView.setShowPosition(positionX,positionY);
            }
        }

        public void setAreaSize(int size){
            if(mengBanView!=null){
                mengBanView.setAreaSize(size);
            }
        }

        public interface MengBanInterface {
            void onAnimationStart(Animator animator);

            void onAnimationEnd(Animator animator);

            void onAnimationCancel(Animator animator);

            void onAnimationRepeat(Animator animator);

            void onAnimationUpdate(ValueAnimator valueAnimator);
        }

        public static AnimatorBuilder build(Activity activity) {
            mengBanView = new MengBanView(activity);
            activity.addContentView(mengBanView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));

            return mengBanView.getAnimator();
        }

    }
}
