package org.ecnu.ryuou;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
public class MainActivity extends BaseActivity {
  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("native-lib");
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.文件夹);
    setSupportActionBar(toolbar);
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @SuppressLint("WrongConstant")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }
  switch (item.getItemId()){
    case R.id.set:
     // Toast.makeText(MainActivity.this ,"设置",0).show();
      Intent intent = new Intent(MainActivity.this, SetActivity.class);
      item.setIntent(intent);
      break;
    case R.id.about:
      //Toast.makeText(MainActivity.this ,"关于",0).show();
      Intent intent2 = new Intent(MainActivity.this, aboutActivity.class);
      item.setIntent(intent2);
      break;
  }
    return super.onOptionsItemSelected(item);
  }




  /**
   * A native method that is implemented by the 'native-lib' native library, which is packaged with
   * this application.
   */
  public native String stringFromJNI();
}
