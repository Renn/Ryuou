package pager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ecnu.ryuou.R;
import org.ecnu.ryuou.adapter.VideoPagerAdapter;
import org.ecnu.ryuou.base.BasePager;
import org.ecnu.ryuou.domain.MediaItem;
import org.ecnu.ryuou.util.LogUtil;

import java.util.ArrayList;

import android.os.Handler;
import android.widget.Toast;

public class VideoPager extends BasePager {

    private ListView listview;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;
    private VideoPagerAdapter videoPagerAdapter;
    private ArrayList<MediaItem> mediaItems;

    public VideoPager(Context context) {
        super(context);
    }

    private Handler handler = new Handler() {
    @Override
        public void handleMessage(Message msg){
        super.handleMessage(msg);
        if(mediaItems!=null &&mediaItems.size()>0){
            videoPagerAdapter = new VideoPagerAdapter(context,mediaItems);


         listview.setAdapter(videoPagerAdapter);
         tv_nomedia.setVisibility(View.GONE);
        }
        else{
         tv_nomedia.setVisibility(View.VISIBLE);
        }
        pb_loading.setVisibility(View.GONE);
    }
    };

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.video_pager,null);
        listview = view.findViewById(R.id.listview);

        tv_nomedia=view.findViewById(R.id.tv_nomedia);
        pb_loading = view.findViewById(R.id.pb_loading);
//        设置item点击事件
        listview.setOnItemClickListener(new MyOnItemClickListener());
        return view;
    }
    class MyOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MediaItem mediaItem =  mediaItems.get(position);
            Toast.makeText(context,"mediaItem=="+mediaItem.toString(),Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void initData() {
        super.initData();
        getDataFromLocal();
    }
//本地sd卡数据
    private void getDataFromLocal() {

        new Thread(){
                @Override
                public void run(){
               super.run();
                    mediaItems=new ArrayList<>();
                    ContentResolver resolver=context.getContentResolver();
                    Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    String[] objs = {
                      MediaStore.Video.Media.DISPLAY_NAME,
                            MediaStore.Video.Media.DURATION,
                            MediaStore.Video.Media.SIZE,
                            MediaStore.Video.Media.DATA,
                            MediaStore.Video.Media.ARTIST
                    };
                    Cursor cursor = resolver.query(uri,objs,null,null,null);
                    if(cursor!=null){
                        while (cursor.moveToNext()){
                            MediaItem mediaItem = new MediaItem();
                            mediaItems.add(mediaItem);

                            String name =cursor.getString(0);
                            mediaItem.setName(name);
                            long duration = cursor.getLong(1);
                            mediaItem.setDuration(duration);
                            long size = cursor.getLong(2);
                            mediaItem.setSize(size);
                            String data = cursor.getString(3);
                            mediaItem.setData(data);
                            String artist = cursor.getString(4);
                            mediaItem.setArtist(artist);
                        }
                        cursor.close();
                    }
                    handler.sendEmptyMessage(0);
        }
        }.start();
    }


}
