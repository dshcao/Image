package com.osshare.image.crop;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.osshare.image.R;
import com.osshare.image.bm.MediaItem;
import com.osshare.image.permission.XPermission;
import com.osshare.image.util.MultiMediaUtil;
import com.osshare.image.view.ImageCropView;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ImageCropActivity extends AppCompatActivity implements View.OnClickListener {

    ImageCropView mCropView;
    TextView tvCrop;
    TextView tvCancel;

    int mAspectX = 1;
    int mAspectY = 1;
    int mOutputX;
    int mOutputY;
    Uri mOutput;

    static final int REQUEST_IMAGE = 111;
    static final int REQUEST_CROP = 112;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_crop);

        init();
    }

    void init() {
        mOutputX = getIntent().getIntExtra("outputX", 350);
        mOutputY = getIntent().getIntExtra("outputY", 350);
        mOutput = getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);

        mCropView = findViewById(R.id.cropView);
        tvCancel = findViewById(R.id.tvCancel);
        tvCrop = findViewById(R.id.tvCrop);

        tvCrop.setOnClickListener(this);
        tvCancel.setOnClickListener(this);


        XPermission.delegate(this).request(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1, new XPermission.Callback() {

            @Override
            public void onGrantedAll(int requestCode, @NotNull String[] permissions) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        Uri uri = data.getData();
        Log.i("fffff", "data.getPath()xx:" + uri.getPath());

        MediaItem mediaItem = MultiMediaUtil.getLocalMedia(getContentResolver(), uri);

        Log.i("fffff", "data.getPath()yy:" + mediaItem.getPath());

        mCropView.setImageURI(Uri.fromFile(new File(mediaItem.getPath())));

//        Intent intent = new Intent(this, ImageCropActivity.class);
//        intent.setDataAndType(Uri.fromFile(new File(mediaItem.getPath())), "image/*");
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        intent.putExtra("outputX", 350);
//        intent.putExtra("outputY", 350);
//        File avatar = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "avatar");
//        Uri outputUri = Uri.fromFile(avatar);//裁剪后输出位置
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
//        startActivityForResult(intent, REQUEST_CROP);
    }


    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.tvCrop) {
            if (mOutput == null) {
                Toast.makeText(this,"裁剪输出位置需要自己设定下",Toast.LENGTH_LONG).show();
                return;
            }
            File file = new File(mOutput.getPath());
            boolean isSuccess = mCropView.crop(file, mOutputX, mOutputY);
            if (!isSuccess) {
                return;
            }
            setResult(Activity.RESULT_OK);
            finish();
        } else if (viewId == R.id.tvCancel) {
            finish();
        }
    }
}
