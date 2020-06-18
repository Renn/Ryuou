package org.ecnu.ryuou.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.ecnu.ryuou.BaseActivity;
import org.ecnu.ryuou.menu.subactivity.CodecActivity;
import org.ecnu.ryuou.menu.subactivity.CommonActivity;
import org.ecnu.ryuou.menu.subactivity.DevolopActivity;
import org.ecnu.ryuou.menu.subactivity.MusicActivity;
import org.ecnu.ryuou.menu.subactivity.PlayerActivity;
import org.ecnu.ryuou.R;
import org.ecnu.ryuou.menu.subactivity.SubtitleActivity;

public class SetActivity extends BaseActivity {
    private String[] data = {"播放器", "解码器", "音频", "字幕", "常规", "开发"};
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SetActivity.this, android.R.layout.simple_list_item_1, data);
        listView = findViewById(R.id.lv);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                          case 0:
                              Intent _intent = new Intent();
                              _intent.setClass(SetActivity.this, PlayerActivity.class);
                              startActivity(_intent);
                               break;
                               case 1:
                                   Intent _intent2 = new Intent();
                                   _intent2.setClass(SetActivity.this, CodecActivity.class);
                                   startActivity(_intent2);
                         break;
                            case 2:
                                Intent _intent3 = new Intent();
                                _intent3.setClass(SetActivity.this, MusicActivity.class);
                                startActivity(_intent3);
                            break;
                            case 3:
                                Intent _intent4 = new Intent();
                                _intent4.setClass(SetActivity.this, SubtitleActivity.class);
                                startActivity(_intent4);
                         break;
                             case 4:
                                 Intent _intent5 = new Intent();
                                 _intent5.setClass(SetActivity.this, CommonActivity.class);
                                 startActivity(_intent5);
                             break;
                        case 5:
                            Intent _intent6 = new Intent();
                            _intent6.setClass(SetActivity.this, DevolopActivity.class);
                            startActivity(_intent6);
                        break;

                }

            }
        });
    }
}


