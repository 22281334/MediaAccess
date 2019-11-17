package comp5216.sydney.edu.au.mediaaccess;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;

public class ViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        String imagePath = getIntent().getStringExtra("path");
        ImageView imagePreview = (ImageView) findViewById(R.id.imageView);
        // Load image from grid view path
        Glide.with(ViewActivity.this).load(new File(imagePath))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imagePreview);

    }

    public void onQuitClick(View view) {
        setResult(RESULT_OK);
        finish();
    }
}
