package com.example.equationsolver;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FILE_PROVIDER_AUTH = "com.example.android.fileprovider";
    FloatingActionButton fabCamera, fabRecapture;
    public static final int RC_CAMERA = 1;
    FirebaseStorage firebaseStorage;
    ProgressDialog dialog;
    ShimmerTextView tvEquation;
    Shimmer shimmer;
    CircleImageView civEquation;
    String mPhotoImagePath, imgUrl;
    boolean isImageCapture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabCamera = findViewById(R.id.fabCamera);
        tvEquation = findViewById(R.id.tvEquation);
        firebaseStorage = FirebaseStorage.getInstance();
        fabRecapture = findViewById(R.id.fabRecapture);
        civEquation = findViewById(R.id.civEquation);
        dialog = new ProgressDialog(this);
        shimmer = new Shimmer();
        shimmer.start(tvEquation);

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isImageCapture) {
                    launchCamera();
                }
                else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Confirmation Required")
                            .setMessage("Are you sure to continue ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    printTheEquation();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                    isImageCapture = false;
                                    launchCamera();
                                }
                            })
                            .create().show();
                }
            }
        });

        fabRecapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isImageCapture) {
                    launchCamera();
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void printTheEquation() {
        fabRecapture.setVisibility(View.GONE);
        fabCamera.setImageResource(R.drawable.camera);
        isImageCapture = false;
        tvEquation.setText("");
        executeTask(imgUrl);
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createTempImageFile(MainActivity.this);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (photoFile != null){
                mPhotoImagePath = photoFile.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(MainActivity.this,
                        FILE_PROVIDER_AUTH,
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                cameraIntent.putExtra("uri-string",photoUri.toString());
            }
            startActivityForResult(cameraIntent, RC_CAMERA);
        }
    }

    public File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalCacheDir();
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == RC_CAMERA && resultCode == RESULT_OK) {
            dialog.setMessage("Uploading...");
            dialog.show();
            Uri selectedImageUri;
            File selectedImageFile;
            if(mPhotoImagePath!=null){
                selectedImageFile = new File(mPhotoImagePath);
                selectedImageUri = Uri.fromFile(selectedImageFile);
            }else{
                selectedImageUri = data.getData();
                selectedImageFile = new File(selectedImageUri.getPath());
            }
            try {
                selectedImageFile = new Compressor(this)
                                    .setQuality(75)
                                    .setMaxHeight(640)
                                    .setMaxWidth(640)
                                    .compressToFile(selectedImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v(TAG,"Uri result : " + selectedImageUri);
            StorageReference storageReference = firebaseStorage.getReference().child(selectedImageUri.getLastPathSegment());
            storageReference.putFile(Uri.fromFile(selectedImageFile))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            StorageReference reference = taskSnapshot.getMetadata().getReference();
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imgUrl = String.valueOf(uri);
                                    dialog.dismiss();
                                    civEquation.setVisibility(View.VISIBLE);
                                    Glide.with(MainActivity.this).load(uri.toString()).into(civEquation);
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
            selectedImageFile.delete();
            fabRecapture.setVisibility(View.VISIBLE);
            fabCamera.setImageResource(R.drawable.ic_check);
            isImageCapture = true;
            mPhotoImagePath = null;
        }
    }

    private void executeTask(String imgUrl) {
        // String testUrl = "https://firebasestorage.googleapis.com/v0/b/letschat-79bc7.appspot.com/o/chat_photos%2Fphoto_19-resized.jpg?alt=media&token=9856e398-85d1-4a98-ab94-a28f2dc44ce6";
        String url = "{\n\n" + "\"url\":" + "\""+ imgUrl + "\"" + "\n\n}";
        GetEquationTask task = new GetEquationTask(this, url, tvEquation, civEquation);
        task.execute("https://boiling-wildwood-98824.herokuapp.com/predict");
    }
}
