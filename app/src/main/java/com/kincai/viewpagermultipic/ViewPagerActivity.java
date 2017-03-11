package com.kincai.viewpagermultipic;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.androidframework.utils.BitmapCompressUtils;
import com.android.androidframework.utils.MD5EncryptorUtils;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.kincai.viewpagermultipic.databinding.ActivityViewpagerBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author KINCAI
 * .
 * description TODO
 * .
 * Time 2016-12-28 14:54
 */
public class ViewPagerActivity extends AppCompatActivity {

    private ActivityViewpagerBinding mBinding;
    private ArrayList<String> mDatas = new ArrayList<>();
    private List<SubsamplingScaleImageView> mViews = new ArrayList<>();
    private String mCacheRootPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_viewpager);

        mCacheRootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/viewpager_cache/";
        Intent intent = getIntent();
        if(intent != null) {
            mDatas = intent.getStringArrayListExtra("data");
        }

        for (int i = 0; i < 4; i++) {
            SubsamplingScaleImageView view = new SubsamplingScaleImageView(this);
            mViews.add(view);
        }

        mBinding.viewpager.setAdapter(new MyAdapter());
    }

    class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT);

            int i = position % 4;
            final SubsamplingScaleImageView imageView = mViews.get(i);
            imageView.setLayoutParams(params);

            final String url = mDatas.get(position);
            String cacheExists = cacheExists(url);
            if(TextUtils.isEmpty(cacheExists)) {//没缓存 需要压缩(压缩耗时 异步)
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... voids) {
                        String cacheNoExistsPath = getCacheNoExistsPath(url);
                        BitmapCompressUtils.compressBitmap(url, cacheNoExistsPath);
                        File file = new File(cacheNoExistsPath);
                        if (file.exists()) {//存在表示成功
                            return cacheNoExistsPath;
                        } else {
                            return url;
                        }
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        imageView.setImage(ImageSource.uri(s));
                    }

                }.execute();


            } else {//有缓存 直接显示
                imageView.setImage(ImageSource.uri(cacheExists));
            }

            container.addView(imageView);
            return imageView;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            int i = position % 4;
            SubsamplingScaleImageView imageView = mViews.get(i);
            if(imageView != null) {
                imageView.recycle();
            }

            container.removeView(imageView);

        }
    }


    /**
     * 判断当前图片url对应的压缩过的缓存是否存在 ""表示不存在
     *
     * @param url 图片路径
     * @return
     */
    private String cacheExists(String url) {
        try {
            File fileDir = new File(mCacheRootPath);
            if(!fileDir.exists()) {
                fileDir.mkdirs();
            }

            File file = new File(mCacheRootPath,new StringBuffer().append(MD5EncryptorUtils.md5Encryption(url)).toString());
            if(file.exists()) {
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public String getCacheNoExistsPath(String url) {
        File fileDir = new File(mCacheRootPath);
        if(!fileDir.exists()) {
            fileDir.mkdirs();
        }


        return new StringBuffer().append(mCacheRootPath)
                .append(MD5EncryptorUtils.md5Encryption(url)).toString();
    }

}
