package org.ecnu.ryuou.pager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import org.ecnu.ryuou.R;
import org.ecnu.ryuou.base.BasePager;
import org.ecnu.ryuou.domain.MediaItem;

public class VideoPager extends BasePager {

  private ListView listview;
  private TextView tv_nomedia;
  private ProgressBar pb_loading;
  private VideoPagerAdapter videoPagerAdapter;
  private ArrayList<MediaItem> mediaItems;
  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (mediaItems != null && mediaItems.size() > 0) {
        videoPagerAdapter = new VideoPagerAdapter(context, mediaItems);

        listview.setAdapter(videoPagerAdapter);
        tv_nomedia.setVisibility(View.GONE);
      } else {
        tv_nomedia.setVisibility(View.VISIBLE);
      }
      pb_loading.setVisibility(View.GONE);
    }
  };

  public VideoPager(Context context) {
    super(context);
  }

  @Override
  public View initView() {
    View view = View.inflate(context, R.layout.video_pager, null);
    listview = view.findViewById(R.id.listview);

    tv_nomedia = view.findViewById(R.id.tv_nomedia);
    pb_loading = view.findViewById(R.id.pb_loading);
//        设置item点击事件
    listview.setOnItemClickListener(new MyOnItemClickListener());
    return view;
  }

  @Override
  public void initData() {
    super.initData();
    getDataFromLocal();
  }

  //本地sd卡数据
  private void getDataFromLocal() {

    new Thread() {
      @Override
      public void run() {
        super.run();
        mediaItems = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] objs = {
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.ARTIST
        };
        Cursor cursor = resolver.query(uri, objs, null, null, null);
        if (cursor != null) {
          while (cursor.moveToNext()) {
            MediaItem mediaItem = new MediaItem();
            mediaItems.add(mediaItem);

            String name = cursor.getString(0);
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

  class MyOnItemClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      MediaItem mediaItem = mediaItems.get(position);
      Toast.makeText(context, "mediaItem==" + mediaItem.toString(), Toast.LENGTH_SHORT).show();
    }
  }

}

class VideoPagerAdapter extends BaseAdapter {

  private final Context context;
  private final ArrayList<MediaItem> mediaItems;

  public VideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems) {
    this.context = context;
    this.mediaItems = mediaItems;
  }

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
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView = View.inflate(context, R.layout.item_video_pager, null);
      viewHolder = new ViewHolder();

      viewHolder.iv_icon = convertView.findViewById(R.id.iv_icon);
      viewHolder.tv_name = convertView.findViewById(R.id.tv_name);
      viewHolder.tv_size = convertView.findViewById(R.id.tv_size);
      viewHolder.tv_time = convertView.findViewById(R.id.tv_time);
      convertView.getTag();
    } else {

      viewHolder = (ViewHolder) convertView.getTag();
    }
    MediaItem mediaItem = mediaItems.get(position);
    viewHolder.tv_name.setText(mediaItem.getName());
//        viewHolder.tv_size.setText((int) mediaItem.getDuration());
    viewHolder.tv_size.setText(Formatter.formatFileSize(context, mediaItem.getSize()));
    return convertView;
  }

  class ViewHolder {

    ImageView iv_icon;
    TextView tv_name;
    TextView tv_time;
    TextView tv_size;
  }
}
