package comp5216.sydney.edu.au.mediaaccess;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    public static final int MY_PERMISSIONS_REQUEST_PICTURE = 100;
    public MyGridViewAdapter adapter;

    // list to store all images from media store
    List<String> paths = new ArrayList<String>();
    private GridView mGridView;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();
        mGridView = (GridView) findViewById(R.id.gridview);

        // get all images from user phone
        getAllImagePath();
        adapter = new MyGridViewAdapter(paths, context);
        mGridView.setAdapter(adapter);
        onItemViewListener();
    }


    public void onItemViewListener() {
        //set up Grid View click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String path = paths.get(arg2);
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                if (intent != null) {
                    // bring the image path to edit image page
                    intent.putExtra("path", path);
                    startActivityForResult(intent, MY_PERMISSIONS_REQUEST_PICTURE);
                }


            }
        });
    }

    /**
     * Take picture button function
     * start new page to camera
     */
    public void onTakePhotoClick(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, MY_PERMISSIONS_REQUEST_PICTURE);
    }

    /**
     * Get result and refresh the Grid view
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if get result refresh the grid view
        if (resultCode==RESULT_OK){
            onCreate(null);
        }
    }


    /**
     * Get all images path
     */
    private void getAllImagePath() {
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        // get all picture from media store
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            // add the images into a list
            if (path.getBytes() != null) {
                paths.add(path);
            }
        }
        // take the newest images to front
        Collections.reverse(paths);
        cursor.close();
    }


}


