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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ecnu.ryuou.R;
import org.ecnu.ryuou.base.BasePager;
import org.ecnu.ryuou.domain.MediaItem;
import org.ecnu.ryuou.util.LogUtil;

import java.util.ArrayList;

import android.os.Handler;

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
            videoPagerAdapter = new VideoPagerAdapter();
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
        return view;
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
    class VideoPagerAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mediaItems.size();
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
            ViewHoder viewHoder;
            if(convertView==null){
                convertView = View.inflate(context,R.layout.item_video_pager,null);
                viewHoder = new ViewHoder();
                viewHoder.iv_icon=convertView.findViewById(R.id.iv_icon);
                viewHoder.tv_name=convertView.findViewById(R.id.tv_name);
                viewHoder.tv_size=convertView.findViewById(R.id.tv_size);
                viewHoder.tv_time=convertView.findViewById(R.id.tv_time);
                convertView.getTag();
            }else{

                viewHoder = (ViewHoder) convertView.getTag();
            }
            MediaItem mediaItem = mediaItems.get(position);
            viewHoder.tv_name.setText(mediaItem.getName());
            viewHoder.tv_size.setText((int) mediaItem.getDuration());
            viewHoder.tv_size.setText(Formatter.formatFileSize(context,mediaItem.getSize()));
            return convertView;
        }
    }
    static class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_time;
        TextView tv_size;
    }
}
