package org.ecnu.ryuou.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ecnu.ryuou.R;
import org.ecnu.ryuou.domain.MediaItem;

import java.util.ArrayList;

import pager.VideoPager;

public class VideoPagerAdapter extends BaseAdapter {
    static class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_time;
        TextView tv_size;
    }
    private final Context context;
    private final ArrayList<MediaItem> mediaItems;

    public VideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems){
     this.context = context;
     this.mediaItems =mediaItems;
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
        ViewHoder viewHoder;
        if(convertView==null){
            convertView = View.inflate(context, R.layout.item_video_pager,null);
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
//        viewHoder.tv_size.setText((int) mediaItem.getDuration());
        viewHoder.tv_size.setText(Formatter.formatFileSize(context,mediaItem.getSize()));
        return convertView;
    }
}
