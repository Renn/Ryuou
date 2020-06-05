package org.ecnu.ryuou.pager;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.ecnu.ryuou.R;
import org.ecnu.ryuou.base.BasePager;
import org.ecnu.ryuou.domain.MediaItem;
//import org.ecnu.ryuou.player.Player;

public class VideoPager extends BasePager {

  private XListView listview;
  private TextView tv_nomedia;
  private ProgressBar pb_loading;
  private SwipeRefreshLayout swipeLayout;

  private VideoPagerAdapter videoPagerAdapter;
  private ArrayList<MediaItem> mediaItems;
  @SuppressLint("HandlerLeak")
  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (mediaItems != null && mediaItems.size() > 0) {
        videoPagerAdapter = new VideoPagerAdapter(context, mediaItems);

        listview.setAdapter(videoPagerAdapter);
        onLoad();
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

  //  time
  public String getSystemTime() {
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    return format.format(new Date());
  }


  @Override
  public View initView() {
    View view = View.inflate(context, R.layout.video_pager, null);
    listview = view.findViewById(R.id.listview);

    tv_nomedia = view.findViewById(R.id.tv_nomedia);
    pb_loading = view.findViewById(R.id.pb_loading);
//        设置item点击事件
    listview.setOnItemClickListener(new MyOnItemClickListener());
    listview.setPullLoadEnable(true);
    listview.setXListViewListener(new myIXListViewListener());
    return view;
  }

  private void onLoad() {
    listview.stopRefresh();
    listview.stopLoadMore();
    listview.setRefreshTime("更新时间：" + getSystemTime());
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

  class myIXListViewListener implements XListView.IXListViewListener {

    @Override
    public void onRefresh() {
      getDataFromLocal();

    }

    @Override
    public void onLoadMore() {

    }
  }

  class MyOnItemClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      MediaItem mediaItem = mediaItems.get(position - 1);
      Toast.makeText(context, "mediaItem==" + mediaItem.toString(), Toast.LENGTH_SHORT).show();
//      调起播放器
      Intent intent = new Intent(context, SystemVideoPlayer.class);
      intent.setDataAndType(Uri.parse(mediaItem.getData()), "video/*");
      context.startActivity(intent);


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
      convertView.setTag(viewHolder);
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
