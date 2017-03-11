package com.kincai.viewpagermultipic;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kincai.viewpagermultipic.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    List<String> mLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setEventListener(new EventListener());
    }

    public class EventListener {
        public void scanPhoto(View view) {
            //扫描相册
            scan();
        }
        public void display(View view) {
            //图片浏览
            if(mLists != null && mLists.size() > 0) {
                startActivity(new Intent(MainActivity.this,ViewPagerActivity.class)
                        .putStringArrayListExtra("data", (ArrayList<String>) mLists));
            } else {
                Toast.makeText(MainActivity.this,"先扫描相册",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 异步扫描相册 这里为了演示 就把所有照片都读取到一起 并没有分相册
     */
    private void scan() {
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... voids) {

                ContentResolver resolver = MainActivity.this.getContentResolver();
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Cursor cursor = resolver.query(imageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + " in(?, ?, ?)",
                        new String[]{"image/jpeg", "image/png","image/gif"},
                        MediaStore.Images.Media.DATE_MODIFIED + " desc");

                List<String> photos = new ArrayList<>();
                if (cursor == null || cursor.getCount() == 0) {
                    return null;
                }
                while (cursor.moveToNext()) {
                    String photoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));//图片路径
                    if(!photos.contains(photoPath)) {
                        photos.add(photoPath);
                    }
                }
                cursor.close();
                return photos;

            }

            @Override
            protected void onPostExecute(List<String> datas) {
                Toast.makeText(MainActivity.this,"扫描完毕 数量："+datas.size(),Toast.LENGTH_SHORT).show();
                mLists = datas;
            }
        }.execute();
    }
}
