package com.kevin.freshlistviewdemo;

import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kevin.freshlistview.FreshListView;

public class FreshListViewDemo extends AppCompatActivity {

    private FreshListView listView;

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            //跟新刷新状态
            listView.updataStatus();
        };
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fresh_list_view_demo);

        //freshlistview控件控件
        listView = (FreshListView) findViewById(R.id.flv_test);

        //设置适配器
        listView.setAdapter(new MyAdapter());

        //监听回调
        listView.setOnRefreshDataLister(new FreshListView.OnRefreshDataLister() {
            @Override
            public void freshData() {
                //测试数据
                mHandler.sendMessageDelayed(mHandler.obtainMessage(), 2000);
            }

            @Override
            public void loadMoreData() {
                //测试数据
                mHandler.sendMessageDelayed(mHandler.obtainMessage(), 2000);
            }
        });


    }

    //设置一个简单的适配器
    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 30;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView tv= new TextView(FreshListViewDemo.this);

            tv.setText("item"+position);

            return tv;
        }
    }
}
