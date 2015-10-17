package com.kevin.freshlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by camch on 2015-10-15- 0015.
 */
public class FreshListView extends ListView {

    private LinearLayout headView;
    private View ll_headfreshview;
    private View footView;
    private int mMeadHeigth;
    private int mFootHeigth;
    private float mDownY = -1;
    private View view;
    private static final int PULL_DOWN_FRESH = 1; //下拉状态
    private static final int PREAPARE_FRESH = 2; //刷新ing
    private static final int ALLREADY_FRESHED = 3; //刷新完成

    private int FRESHSTATUS = PULL_DOWN_FRESH;
    private ImageView iv_arrow;
    private ProgressBar pb;
    private TextView tv_freshText;
    private TextView tv_time;
    private RotateAnimation downAi;
    private RotateAnimation upAni;
    private boolean isLoadingMore;

    public FreshListView(Context context) {
        this(context, null);
    }

    public FreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHead();
        initFoot();
        initAnimation();
        initEvent();
    }

    //事件处理
    private void initEvent() {
        this.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //最后显示的item
                int lastVisiblePosition = getLastVisiblePosition();

                //总共的item数量
                int totalCount = getAdapter().getCount();

                //静止状态
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && !isLoadingMore) {
                    if (lastVisiblePosition == totalCount - 1) {
                        //说明是最后一条数据,显示加载更多
                        footView.setPadding(0, 0, 0, 0);

                        //处理回调
                        if (onRefreshDataLister != null) {
                            onRefreshDataLister.loadMoreData();
                        }
                        //改变状态
                        isLoadingMore = true;
                    }
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //太灵敏了 滑动就调用
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //覆盖touch
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN://按下
                mDownY = ev.getY();//按下位置的Y坐标

                break;
            case MotionEvent.ACTION_MOVE://移动

                //如果正在刷新状态
                if (FRESHSTATUS == ALLREADY_FRESHED) {
                    return true;
                }


                if (mDownY == -1) {
                    mDownY = ev.getY();
                }
                float moveY = ev.getY();

                float dy = moveY - mDownY;
                //1.下拉,轮播图完全显示
                if (getFirstVisiblePosition() == 0 && dy > 0) {
                    //显示刷新的view
                    float d = dy - mMeadHeigth;
                    if (d >= 0 && FRESHSTATUS != PREAPARE_FRESH) //松开刷新
                    {
                        //如果是松开刷新状态就不进来
                        FRESHSTATUS = PREAPARE_FRESH;
                        processStatus();
                    } else if (d < 0 && FRESHSTATUS != PULL_DOWN_FRESH) { //下拉刷新
                        //如果是下拉刷新状态就不进来
                        FRESHSTATUS = PULL_DOWN_FRESH;
                        processStatus();
                    }
                    ll_headfreshview.setPadding(0,
                            (int) dy - mMeadHeigth > 0 ? 0 : (int) dy - mMeadHeigth, //三元运算符设置边界,下拉最大位置为0
                            0, 0);

                    return true;//自己处理触摸事件
                }
                mDownY = moveY;//改变初始位置

                break;
            case MotionEvent.ACTION_UP://松开

                //判断状态
                if (FRESHSTATUS == PULL_DOWN_FRESH)//下拉刷新
                {
                    //隐藏
                    ll_headfreshview.setPadding(0, -mMeadHeigth, 0, 0);
                } else if (FRESHSTATUS == PREAPARE_FRESH) //松开刷新
                {

                    ll_headfreshview.setPadding(0, 0, 0, 0);
                    FRESHSTATUS = ALLREADY_FRESHED; //改变状态

                    //更新状态
                    processStatus();

                    //刷新数据业务回掉

                    if (onRefreshDataLister != null) {
                        onRefreshDataLister.freshData();
                    }
                }
                break;

        }


        return super.onTouchEvent(ev);
    }

    public void updataStatus() {
        if (isLoadingMore) {
            //说明是加载更多的状态
            //隐藏加载更多
            footView.setPadding(0, -mFootHeigth, 0, 0);
            Toast.makeText(getContext(), "加载更多完成！", Toast.LENGTH_SHORT).show();
            isLoadingMore = false;//改变状态
        } else {
            updataRefreshStatus();
        }
    }

    /**
     * 刷新下拉更新的状态
     */

    public void updataRefreshStatus() {
        //改变文字
        FRESHSTATUS = PULL_DOWN_FRESH;
        //显示箭头
        iv_arrow.setVisibility(View.VISIBLE);
        //隐藏进度条
        pb.setVisibility(View.GONE);
        //改变文字
        tv_freshText.setText("下拉刷新");
        //设置时间
        tv_time.setText(getCurrentTime());
        //隐藏
        ll_headfreshview.setPadding(0, -mMeadHeigth, 0, 0);

        Toast.makeText(getContext(), "刷新完成！", Toast.LENGTH_SHORT).show();
    }

    //获取当前的时间
    private String getCurrentTime() {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return format.format(new Date());
    }

    /**
     * 处理下拉刷新的状态
     */
    private void processStatus() {
        switch (FRESHSTATUS) {
            case PULL_DOWN_FRESH: //下拉状态
                //1.隐藏进度条
                //2.显示下拉箭头,
                // 3.设置正在刷新的文字
                //4.隐藏时间
                //1.箭头动画

                iv_arrow.startAnimation(downAi);
                tv_freshText.setText("下拉刷新");


                break;
            case PREAPARE_FRESH://松开刷新
                //1.隐藏箭头

                //2.显示进度条
                //3.设置文字,正在刷新
                //4.刷新完成
                iv_arrow.startAnimation(upAni);
                tv_freshText.setText("松开刷新");

                break;
            case ALLREADY_FRESHED: //刷新完成
                //1.清除箭头动画
                iv_arrow.clearAnimation();

                //2.隐藏箭头
                iv_arrow.setVisibility(View.GONE);

                //3.显示进度条
                pb.setVisibility(View.VISIBLE);

                //4. 改变文字
                tv_freshText.setText("正在刷新···");

                break;

        }
    }

    //刷新数据的回调
    public OnRefreshDataLister onRefreshDataLister;

    public void setOnRefreshDataLister(OnRefreshDataLister lister) {
        this.onRefreshDataLister = lister;
    }


    public interface OnRefreshDataLister {
        void freshData();

        void loadMoreData();
    }


    //初始化下拉刷新的动画
    private void initAnimation() {
        upAni = new RotateAnimation(0, -180,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        upAni.setDuration(500);
        upAni.setFillAfter(true);//停留在动画结束的位置

        downAi = new RotateAnimation(-180, -360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        downAi.setDuration(500);
        downAi.setFillAfter(true);//停留在动画结束的位置


    }


    private void initFoot() {
        footView = View.inflate(getContext(), R.layout.footfresh, null);

        footView.measure(0, 0);//测量

        mFootHeigth = footView.getMeasuredHeight();//高度

        footView.setPadding(0, -mFootHeigth, 0, 0);//隐藏


        addFooterView(footView);
    }

    //初始化头部
    private void initHead() {
        headView = (LinearLayout) View.inflate(getContext(), R.layout.headfresh, null);

        ll_headfreshview = headView.findViewById(R.id.ll_headfreshview);

        //箭头
        iv_arrow = (ImageView) headView.findViewById(R.id.headfresh_imageview_arrow);
        //圈圈 进度条
        pb = (ProgressBar) headView.findViewById(R.id.headfresh_progressbar);

        //下拉刷新的文字
        tv_freshText = (TextView) headView.findViewById(R.id.headfresh_textview_title);

        //时间
        tv_time = (TextView) headView.findViewById(R.id.headfresh_textview_time);

        ll_headfreshview.measure(0, 0);//测量

        mMeadHeigth = ll_headfreshview.getMeasuredHeight();//高度

        ll_headfreshview.setPadding(0, -mMeadHeigth, 0, 0);//隐藏


        //刷新的部分添加到第一个
        addHeaderView(headView);


    }

    /**
     * 添加View的功能
     *
     * @param view
     */
    public void addViewPagerView(View view) {

        this.view = view;
        //添加view控件到linearlayout中
        headView.addView(view);
    }

}
