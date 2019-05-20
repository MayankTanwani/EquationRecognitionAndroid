package com.example.equationsolver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity implements SolutionDialog.OnPositiveListener {

    Toolbar toolbar;
    RecyclerView rvListEquations;
    FloatingActionButton fabCamera, fabSolution;
    TextView tvInfo;
    FirebaseStorage firebaseStorage;
    ProgressDialog dialog;
    String mPhotoImagePath, imgUrl;
    ArrayList<Equation> equationArrayList;
    EquationAdapter equationAdapter;
    boolean updateEquationFlag = false;
    int equationPosition = -1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FILE_PROVIDER_AUTH = "com.example.android.fileprovider";
    public static final int RC_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        toolbar = findViewById(R.id.tbMain);
        rvListEquations = findViewById(R.id.rvListEquations);
        fabCamera = findViewById(R.id.fabCamera);
        fabSolution = findViewById(R.id.fabSolution);
        tvInfo = findViewById(R.id.tvInfo);
        setSupportActionBar(toolbar);
        rvListEquations.setLayoutManager(new LinearLayoutManager(this));
        initObjects();
    }

    private void initObjects() {
        firebaseStorage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(this);
        equationArrayList = new ArrayList<>();
        equationAdapter = new EquationAdapter(this, equationArrayList);
        fabCamera.setOnClickListener(cameraListener);
        fabSolution.setOnClickListener(solutionListener);
        rvListEquations.setAdapter(equationAdapter);
        equationAdapter.setOnEquationClickListener(new EquationAdapter.OnEquationClickListener() {
            @Override
            public void getPosition(int i) {
                updateEquationFlag = true;
                equationPosition = i;
                launchCamera();
            }
        });
        equationAdapter.setOnEquationFabClickListener(new EquationAdapter.OnEquationFabClickListener() {
            @Override
            public void getReferences(int i, TextView tvToChange, String equation) {
                Log.d(TAG, "Array List Position: " + i);
                String url = equationArrayList.get(i).getImgUri();
                equationArrayList.remove(i);
                equationArrayList.add(new Equation(url, equation));
                equationAdapter.updateEquations(equationArrayList);
            }
        });
    }

    View.OnClickListener cameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            launchCamera();
        }
    };

    View.OnClickListener solutionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String equations = "";
            for(int i = 0 ; i < equationArrayList.size() ; i++) {
                equations += equationArrayList.get(i).getEquation();
                if(i != equationArrayList.size() - 1)
                    equations += ";";
            }
            String sampleUrl = "{\n" +
                    "\"equations\"" +
                    ":" + "\"4x+3y=10;y=2\"" + "\n}";
            String finalUrl = "{\n" +
                    "\"equations\"" +
                    ":" + "\"" + equations + "\"" + "\n}";
            Log.d(TAG, "Sample Url: " + sampleUrl);
            Log.d(TAG, "Final Url: " + finalUrl);
            DownloadSolutionTask task = new DownloadSolutionTask(MainActivity.this, finalUrl, new DownloadSolutionTask.OnDownloadSolution() {
                @Override
                public void getSolution(Solution result) {
                    Log.d(TAG, "Result of Url: " + result.getGraph());
                    SolutionDialog dialog = new SolutionDialog();
                    dialog.setEquationsAndSolution(MainActivity.this, equationArrayList, result);
                    dialog.show(getSupportFragmentManager(), "DIALOG");

                }
            });
            task.execute("https://equationsolverwolfram.herokuapp.com/");
        }
    };

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
                        .setMaxHeight(640)
                        .setMaxWidth(480)
                        .setQuality(50)
                        .compressToFile(selectedImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v(TAG,"Uri result : " + selectedImageUri);
            StorageReference storageReference = firebaseStorage.getReference().child(selectedImageUri.getLastPathSegment());
            storageReference.putFile(Uri.fromFile(selectedImageFile)).addOnSuccessListener(imageStorageListener);
            // Delete the temp image file
            selectedImageFile.delete();
            mPhotoImagePath = null;
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

    OnSuccessListener<UploadTask.TaskSnapshot> imageStorageListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            StorageReference reference = taskSnapshot.getMetadata().getReference();
            getDownloadImage(reference);
        }
    };

    private void getDownloadImage(StorageReference reference) {
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imgUrl = String.valueOf(uri);
                Log.d(TAG, "Image Uri: " + imgUrl);
                dialog.dismiss();
                executeTask();
            }
        });
    }

    private void executeTask() {
        // String testUrl = "https://firebasestorage.googleapis.com/v0/b/letschat-79bc7.appspot.com/o/chat_photos%2Fphoto_19-resized.jpg?alt=media&token=9856e398-85d1-4a98-ab94-a28f2dc44ce6";
        String url = "{\n\n" + "\"url\":" + "\""+ imgUrl + "\"" + "\n\n}";
        GetEquationTask task = new GetEquationTask(this, url, new GetEquationTask.OnDownloadEquation() {
            @Override
            public void getStringEquation(String equation) {
                if(!equation.equals("null")) {
                    tvInfo.setVisibility(View.VISIBLE);
                    if(!updateEquationFlag && equationPosition == -1) {
                        // Insert the record
                        equationArrayList.add(new Equation(imgUrl, equation));
                        equationAdapter.updateEquations(equationArrayList);
                    }
                    else {
                        // Update the record
                        equationArrayList.remove(equationPosition);
                        equationArrayList.add(equationPosition, new Equation(imgUrl, equation));
                        equationAdapter.notifyDataSetChanged();
                    }
                }
                else {
                    Snackbar.make(rvListEquations, "Not able to recognize", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        // New Equation Recognition API Url
        task.execute("https://equationrecognitionapi.herokuapp.com/predict");
    }

    @Override
    public void clearArrayList(boolean flag) {
        if(flag) {
            equationArrayList.clear();
            equationAdapter.updateEquations(equationArrayList);
            tvInfo.setVisibility(View.GONE);
        }
    }
}
