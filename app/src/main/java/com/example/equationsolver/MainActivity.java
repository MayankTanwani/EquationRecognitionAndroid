package com.example.equationsolver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fabCamera;
    public static final int RC_CAMERA = 1;
    FirebaseStorage firebaseStorage;
    ProgressDialog dialog;
    TextView tvEquation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabCamera = findViewById(R.id.fabCamera);
        tvEquation = findViewById(R.id.tvEquation);
        firebaseStorage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(this);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, RC_CAMERA);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == RC_CAMERA && resultCode == RESULT_OK) {
            dialog.setMessage("Uploading...");
            dialog.show();
            Bundle bundle = data.getExtras();
            Bitmap imageBitmap = (Bitmap) bundle.get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();
            StorageReference storageReference = firebaseStorage.getReference().child("equation.jpg");
            storageReference.putBytes(bytes)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            StorageReference reference = taskSnapshot.getMetadata().getReference();
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imgUrl = String.valueOf(uri);
                                    dialog.dismiss();
                                    executeTask(imgUrl);
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
        }
    }

    private void executeTask(String imgUrl) {
        String testUrl = "https://firebasestorage.googleapis.com/v0/b/letschat-79bc7.appspot.com/o/chat_photos%2Fphoto_19-resized.jpg?alt=media&token=9856e398-85d1-4a98-ab94-a28f2dc44ce6";
        String url = "{\n\n" + "\"url\":" + "\""+ testUrl + "\"" + "\n\n}";
        GetEquationTask task = new GetEquationTask(this, url, tvEquation);
        task.execute("https://boiling-wildwood-98824.herokuapp.com/predict");
    }
}
