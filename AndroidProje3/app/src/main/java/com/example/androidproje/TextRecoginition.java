package com.example.androidproje;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.InputStream;
import java.util.List;

public class TextRecoginition extends AppCompatActivity implements View.OnClickListener
{
    LinearLayout cameraLayout, detectLayout, galleryLayout;
    ImageView imgView;
    TextView textArea;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_recoginition);
        detectLayout = findViewById(R.id.detect);
        cameraLayout = findViewById(R.id.camera);
        galleryLayout = findViewById(R.id.gallery);

        imgView = findViewById(R.id.image);
        textArea = findViewById(R.id.text);

        detectLayout.setOnClickListener(this);
        cameraLayout.setOnClickListener(this);
        galleryLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId()) {
            case R.id.detect:
                try {
                    runTextRecognition();
                }catch (Exception e){
                    Toast.makeText(TextRecoginition.this, "Görsel Seçilmedi", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.camera:
                askCameraPermissions();
                break;
            case R.id.gallery:
                galleryAddPic();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }


    }
    //Kamera Fonksiyonları
    static final int REQUEST_IMAGE = 0;
    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE);
    }
    static final int CAMERA_PERM = 2;

    private void askCameraPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA
            },CAMERA_PERM);
        }
        else {
            dispatchTakePictureIntent();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            dispatchTakePictureIntent();
        }
        else{
            Toast.makeText(this, "Kamera İzni Kabul Edilmedi", Toast.LENGTH_SHORT).show();
        }
    }
    // Galeri Fonksiyonları
    static final int REQUEST_GALLERY = 1;

    private void galleryAddPic(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(mediaScanIntent, REQUEST_GALLERY);
    }
    Bitmap galleryImage;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_IMAGE:
                if (resultCode == RESULT_OK){
                    Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                    imgView.setImageBitmap(selectedImage);
                }
                break;
            case REQUEST_GALLERY:
                try {
                    Uri imageUri = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    galleryImage = BitmapFactory.decodeStream(imageStream);
                    imgView.setImageBitmap(galleryImage);

                }catch (Exception e){
                    Toast.makeText(this, "Görsel Galeriden Alınamadı",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }
    // Text Tespit İşlemleri

    private void runTextRecognition(){
        Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();
        int rotationDegree = 0;

        InputImage image = InputImage.fromBitmap(bitmap, rotationDegree);

        TextRecognizer recognizer = TextRecognition.getClient();

        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                processTextRecognition(visionText);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(TextRecoginition.this, "Başarısız İşlem",Toast.LENGTH_SHORT).show();
                                    }
                                });
    }

    private void processTextRecognition(Text visionText)
    {
        List<Text.TextBlock> blocks = visionText.getTextBlocks();
        if (blocks.size() == 0){
            Toast.makeText(TextRecoginition.this, "Görselde Metin Tespit Edilemedi",Toast.LENGTH_LONG).show();
        }

        StringBuilder text = new StringBuilder();

        for (int i = 0; i<blocks.size();i++){
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j<lines.size();j++){
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k<elements.size();k++){
                    text.append(elements.get(k).getText() + " ");
                }
            }
        }
        textArea.setText(text);

    }
}
