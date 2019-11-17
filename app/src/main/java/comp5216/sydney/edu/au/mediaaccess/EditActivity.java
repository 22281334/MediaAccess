package comp5216.sydney.edu.au.mediaaccess;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;


public class EditActivity extends AppCompatActivity {
    private ImageView editImagePreview;
    String imagePath;
    private File imageViewFile;
    private Uri uri;
    private ArrayList<Bitmap> editBitMapList = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        imagePath = getIntent().getStringExtra("path");
        imageViewFile = new File(imagePath);
        uri = Uri.fromFile(imageViewFile);
        editImagePreview = (ImageView) findViewById(R.id.editImageView);

        editBitMapList.add(uriToBitmap(uri));
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            editBitMapList.add(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Glide.with(EditActivity.this)
                .load(getNewestBitmap())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(editImagePreview);
    }

    private Bitmap getNewestBitmap() {
        return editBitMapList.get(editBitMapList.size() - 1);
    }

    public void onUndoClick(View view) {
        if (editBitMapList.size() != 0) {
            editBitMapList.remove(editBitMapList.size() - 1);
            if (editBitMapList.size() != 0) {
                Glide.with(EditActivity.this)
                        .load(getNewestBitmap())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(editImagePreview);
            }
        } else {
            Toast.makeText(EditActivity.this, "Already returned to the original photo", Toast.LENGTH_SHORT).show();
        }

    }

    public void onGreyyingClick(View view) {
        Bitmap bitmap = ((BitmapDrawable) editImagePreview.getDrawable().getCurrent()).getBitmap();
        bitmap = getGrayBitmap(bitmap);
        editBitMapList.add(bitmap);
        Glide.with(EditActivity.this)
                .asBitmap()
                .load(getNewestBitmap())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(editImagePreview);

    }


    public static Bitmap getGrayBitmap(Bitmap bm) {
        Bitmap bitmap = null;
        //get images width and height
        int width = bm.getWidth();
        int height = bm.getHeight();
        //create grey images
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix matrix = new ColorMatrix();
        //set up saturation 0 for grey,1 for original image
        matrix.setSaturation(0);
        ColorMatrixColorFilter cmcf = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(cmcf);
        canvas.drawBitmap(bm, 0, 0, paint);
        return bitmap;
    }


    public void onCropClick(View view) {
        if (editBitMapList.size() != 0) {
            cropRawPhoto(uri);
        } else {
            cropRawPhoto(Uri.fromFile(imageViewFile));
        }
    }

    public void cropRawPhoto(Uri uri) {

        Uri sourceUri = uri;
        File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        File outFile = new File(outDir, System.currentTimeMillis() + ".jpg");
        String cameraScalePath = outFile.getAbsolutePath();
        Uri destinationUri = Uri.fromFile(outFile);
        UCrop uCrop = UCrop.of(sourceUri, destinationUri);
        UCrop.Options options = new UCrop.Options();
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        options.setHideBottomControls(false);
        //change toolbar color
        options.setToolbarColor(ActivityCompat.getColor(this, R.color.colorPrimary));
        options.setStatusBarColor(ActivityCompat.getColor(this, R.color.colorPrimary));
        options.setFreeStyleCropEnabled(true);
        options.setToolbarTitle("Cropping");
        uCrop.withOptions(options);
        //start crop page
        uCrop.start(this, UCrop.REQUEST_CROP);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // get the result
        if (resultCode == RESULT_OK) {
            final Uri resultUri = UCrop.getOutput(data);
            // add result to bitmap list
            editBitMapList.add(uriToBitmap(resultUri));
            Glide.with(EditActivity.this)
                    .load(resultUri)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(editImagePreview);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }


    /**
     * Save edit result to local storage
     */
    private void save(byte[] bytes) throws IOException {
        File file = imageViewFile;
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
        } finally {
            if (outputStream != null)
                outputStream.close();
        }
        // notify the media to update
        this.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(file.getPath()))));
    }

    /**
     * change uri to bitmap
     */
    private Bitmap uriToBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Change bitmap to bytes array
     */
    private byte[] bitmapToByte(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
        return null;
    }


    public void onSaveClick(View view) {
        if (uri != null) {
            byte[] bytes = bitmapToByte(getNewestBitmap());
            try {
                save(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setResult(RESULT_OK);
        finish();
    }

    public void onCancelClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
        builder.setTitle(R.string.dialog_cancel_title)
                .setMessage(R.string.dialog_cancel_msg)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    // click yes to finish page
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent data = new Intent();
                        setResult(RESULT_OK, data); // set result code and bundle data for response
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User cancelled the dialog
                        // Nothing happens
                    }
                });
        builder.create().show();
    }
}
