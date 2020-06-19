package org.ecnu.ryuou.video;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.ecnu.ryuou.R;
import org.ecnu.ryuou.base.BasePager;
import org.ecnu.ryuou.util.LogUtil;
import org.ecnu.ryuou.util.MediaScanner;

public class VideoPager extends BasePager {

  private static final String TAG = "VideoPager";

  /* list of all available videos */
  private XListView listView;
  private ArrayList<MediaItem> mediaItems;
  private VideoPagerAdapter videoPagerAdapter;

  /* tip in case no video available */
  private TextView tvNomedia;

  private ProgressBar pbLoading;

  //private Context context;

  @SuppressLint("HandlerLeak")
  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (mediaItems != null && mediaItems.size() > 0) {
        videoPagerAdapter = new VideoPagerAdapter(context, mediaItems);
        listView.setAdapter(videoPagerAdapter);
        onLoad();
        tvNomedia.setVisibility(View.GONE);
      } else {
        tvNomedia.setVisibility(View.VISIBLE);
      }
      pbLoading.setVisibility(View.GONE);
    }
  };

  public VideoPager(Context context) {
    super(context);
    //this.context = context;
    mediaItems = new ArrayList<>();
  }

  private String getSystemTime() {
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    return format.format(new Date());
  }

  @Override
  public View initView() {
    View view = View.inflate(context, R.layout.video_pager, null);
    listView = view.findViewById(R.id.listview);

    tvNomedia = view.findViewById(R.id.tv_nomedia);
    pbLoading = view.findViewById(R.id.pb_loading);

    listView.setOnItemClickListener(new MyOnItemClickListener());
    listView.setPullLoadEnable(false);
    listView.setXListViewListener(new myIXListViewListener());

    return view;
  }

  private void onLoad() {
    LogUtil.d("videopager", "on load");
    listView.stopRefresh();
    listView.stopLoadMore();
    listView.setRefreshTime("更新时间：" + getSystemTime());
  }

  @Override
  public void initData() {
    super.initData();
    LogUtil.d("videopager", "init data");
    getDataFromLocal();
  }

  //本地sd卡数据
  private void getDataFromLocal() {

    new Thread() {
      @Override
      public void run() {
        super.run();

        mediaItems.clear();
        scanNewFile();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        LogUtil.d("videopager", "finding items " + uri.toString());
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

            mediaItems.add(mediaItem);
          }
          cursor.close();
        }
        handler.sendEmptyMessage(0);
        LogUtil.d("videopager", "finding items " + mediaItems.size());
      }
    }.start();
  }

  private void scanNewFile() {
    LogUtil.d("videopager", "scanning file in vp");
    MediaScanner ms = MediaScanner.getInstace();
    MediaScanner.ScanFile sf = new MediaScanner.ScanFile(Environment.getExternalStorageDirectory().getPath()
        + File.separator + "Download" + File.separator, "media/*");
    ms.scanFile(context, sf);
  }

  class myIXListViewListener implements XListView.IXListViewListener {

    @Override
    public void onRefresh() {
      LogUtil.d("videopager", "xlist listener on refresh " + mediaItems.size());
      getDataFromLocal();
      videoPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadMore() {

    }
  }

  class MyOnItemClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      MediaItem mediaItem = mediaItems.get(position - 1);
//      Toast.makeText(context, "mediaItem==" + mediaItem.toString(), Toast.LENGTH_SHORT).show();
//      调起播放器
      Intent intent = new Intent(context, SystemVideoPlayer.class);
      intent.setDataAndType(Uri.parse(mediaItem.getData()), "video/*");
      LogUtil.d(TAG, "mediaItem.getData()=" + mediaItem.getData());
      LogUtil.d(TAG, "URI.toString()=" + Uri.parse(mediaItem.getData()));
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
    viewHolder.tv_time.setText(mediaItem.getFormattedDuration());
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
