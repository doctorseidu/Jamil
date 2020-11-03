 package com.nnems.jamil;

 import android.app.DownloadManager;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;

 import androidx.annotation.NonNull;
 import androidx.appcompat.app.AlertDialog;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.core.content.ContextCompat;


 import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
 import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
 import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
 import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
 import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
 import com.google.android.material.floatingactionbutton.FloatingActionButton;
 import com.google.gson.JsonObject;

 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Objects;

 import retrofit2.Call;
 import retrofit2.Callback;
 import retrofit2.Response;
 import retrofit2.Retrofit;
 import retrofit2.converter.gson.GsonConverterFactory;

 import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
 import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;




 public class MainActivity extends AppCompatActivity {

     private TextView mAyatArabicTV,mAyatEnglishTV, mAyatNameTV;
    private LinearLayout mLinearLayout;
    private FloatingActionButton mSaveButton, mSetDataBtn;
    private Bitmap mBitmap;

    private AlertDialog.Builder dialog;
    private AlertDialog show;

    private Spinner mEnglishTranslationSpinner;
    private Spinner mRecitersSpinner;

    public static UserData mUserData = new UserData("","",null ,null);

    private String mChapter;
    private String mVerse;
    private QuranApi mQuranApi;


     private MediaPlayer mMediaPlayer;
     private Handler mHandler = new Handler();
     private String mAudioLink = "";

     private boolean playPause;
     private ProgressDialog progressDialog;
     private boolean initialStage = true;

     private boolean setStopIcon;
     private MenuItem mMenuItem;
     private String mFileImageName;

     private FFmpeg mFFmpeg;
     private String[] commands;
     private String mImageFilePath;
     private String mAudioFilePath;
     private String mVideoFilePath;


     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAyatArabicTV = findViewById(R.id.arabic_text_view);
        mAyatEnglishTV = findViewById(R.id.english_text_view);
        mAyatNameTV = findViewById(R.id.ayat_verse_chapter_text_view);
        mLinearLayout = findViewById(R.id.ayat_linearlayout);
        mSaveButton = findViewById(R.id.fab_save);
        mSetDataBtn = findViewById(R.id.fab_set_data);
         progressDialog = new ProgressDialog(this);

         mMediaPlayer = new MediaPlayer();


             String imgDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString()+"/"+mFileImageName+".mp4";

                mVideoFilePath = imgDir;


         mFFmpeg = FFmpeg.getInstance(this);
//
//         loadFFmpeg();




         Retrofit retrofit = new Retrofit.Builder()
                 .baseUrl(QuranApi.BASE_URL)
                 .addConverterFactory(GsonConverterFactory.create())
                 .build();

         mQuranApi = retrofit.create(QuranApi.class);



        mLinearLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));



        mSetDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.search_ayat,null);
                dialog.setView(dialogView);

                final EditText chapterEditTextView = dialogView.findViewById(R.id.chapter_editTextView);
                final EditText verseEditTextView = dialogView.findViewById(R.id.verse_editTextView);

                chapterEditTextView.setText(mUserData.getChapter());
                verseEditTextView.setText(mUserData.getVerse());

                //              SETTING UP TRANSALATIONS SPINNER

                mEnglishTranslationSpinner = dialogView.findViewById(R.id.spinner_select_english_translation);
                ArrayAdapter<CharSequence> englishTranslationAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                        R.array.english_translations, android.R.layout.simple_spinner_item);
                englishTranslationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mEnglishTranslationSpinner.setAdapter(englishTranslationAdapter);

//              SETTING UP RECITERS SPINNER
                mRecitersSpinner = dialogView.findViewById(R.id.spinner_select_reciter);
                ArrayAdapter<CharSequence> recitersAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                        R.array.reciters, android.R.layout.simple_spinner_item);
                recitersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mRecitersSpinner.setAdapter(recitersAdapter);

                Button okBtn = dialogView.findViewById(R.id.button_ok);
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mChapter = chapterEditTextView.getText().toString();
                        mVerse = verseEditTextView.getText().toString();

                        if(mChapter.equalsIgnoreCase("") || mVerse.equalsIgnoreCase("")){

                            Toast.makeText(MainActivity.this, "Enter Verse & Chapter", Toast.LENGTH_SHORT).show();

                        } else{

                            mUserData = new UserData(mChapter,mVerse,null,null);

                            Log.d("ayat",mUserData.getAyat());

                            show.dismiss();

                            getArabicTextData(mQuranApi);

                            getEnglishTextData(mQuranApi);

                            getAudioData(mQuranApi);


                        }


                    }
                });
                show = dialog.show();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dialog = new AlertDialog.Builder(MainActivity.this);
//                LayoutInflater inflater = getLayoutInflater();
//                final View dialogView = inflater.inflate(R.layout.layout_image, null);
//                dialog.setView(dialogView);
//
//                ImageView imageView = dialogView.findViewById(R.id.layout_IV);

                mBitmap = convertViewToImage(mLinearLayout/*,imageView*/);

//                show = dialog.show();

                if(checkPermission()){
                    try {
                        saveBitmapToDevice();

                        String audioDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();

                        downloadFile(MainActivity.this,mFileImageName,".mp3",audioDir,mAudioLink);

                        mAudioFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()+"/"+mFileImageName+".mp3";
                        mImageFilePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/"+mFileImageName+".jpg";

//                        ffmpeg -loop 1 -i inputimage.jpg -i +mA -c:v libx264 -c:a aac -strict experimental -b:a 192k -shortest output.mp4

                        commands = new String[]{"ffmpeg -loop 1",
                                "-i",mImageFilePath,"-i",mAudioFilePath,
                                "-c:v libx264 -c:a aac -strict experimental -b:a 192k -shortest",
                                mVideoFilePath};

                        executeFFmpegCommand(commands);
//
//                        int rc = FFmpeg.execute(commands);
//
//                        if (rc == RETURN_CODE_SUCCESS) {
//                            Log.i(Config.TAG, "Command execution completed successfully.");
//                        } else if (rc == RETURN_CODE_CANCEL) {
//                            Log.i(Config.TAG, "Command execution cancelled by user.");
//                        } else {
//                            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
//                            Config.printLastCommandOutput(Log.INFO);
//                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        requestPermissions(new String[]{
                                WRITE_EXTERNAL_STORAGE,
                                READ_EXTERNAL_STORAGE
                        },111);
                    }
                }




            }
        });

    }

     private void loadFFmpeg() {
         try {
             mFFmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                 @Override
                 public void onFailure() {

                     Toast.makeText(MainActivity.this, "FFmpeg Library Failed to Load",
                             Toast.LENGTH_SHORT).show();

                 }

                 @Override
                 public void onSuccess() {
                     Toast.makeText(MainActivity.this, "FFmpeg Library Loaded Succesfully",
                             Toast.LENGTH_SHORT).show();
                 }

                 @Override
                 public void onStart() {

                 }

                 @Override
                 public void onFinish() {

                 }
             });
         } catch (FFmpegNotSupportedException e) {

         }
     }

     private void executeFFmpegCommand( String[] cmd){
         try {
             mFFmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                 @Override
                 public void onSuccess(String message) {
                     Toast.makeText(MainActivity.this, "successful", Toast.LENGTH_SHORT).show();

                 }

                 @Override
                 public void onProgress(String message) {

                 }

                 @Override
                 public void onFailure(String message) {
                     Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();

                 }

                 @Override
                 public void onStart() {

                 }

                 @Override
                 public void onFinish() {

                 }
             });
         } catch (FFmpegCommandAlreadyRunningException e) {
             e.printStackTrace();
         }
     }

     private boolean checkPermission(){
        int write = ContextCompat.checkSelfPermission(this,WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(this,READ_EXTERNAL_STORAGE);

        return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

       for(int grantResult : grantResults){
           if(grantResult == PackageManager.PERMISSION_GRANTED){
               Toast.makeText(this, "Permission Granted!!", Toast.LENGTH_SHORT).show();
           }
        }
    }

    private void saveBitmapToDevice() throws IOException {

        OutputStream fos;

        String name = mFileImageName;



            String imgDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File image = new File(imgDir,name+".jpg");
            fos = new FileOutputStream(image);

        mBitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Bitmap convertViewToImage(LinearLayout linearLayout/*, ImageView imageView*/) {
        Bitmap bitmap = Bitmap.createBitmap(linearLayout.getWidth(), linearLayout.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas= new Canvas(bitmap);

        canvas.drawColor(getResources().getColor(android.R.color.holo_blue_bright));

        linearLayout.draw(canvas);

//        imageView.setImageBitmap(bitmap);

        return bitmap;
    }


     private void getArabicTextData(QuranApi quranApi) {

         Call<ApiData> call = quranApi.getAyatArabicText(mUserData.getAyat());

         call.enqueue(new Callback<ApiData>() {
             @Override
             public void onResponse(Call<ApiData> call, Response<ApiData> response) {

                 ApiData apiData;

                 apiData = response.body();

                 JsonObject jsonObject = apiData.data;

                 String text = jsonObject.get("text").toString();

                 Log.d("AYAT ", text);

                 String[] data = text.split("\"");

                 mAyatArabicTV.setText(data[1]);
             }

             @Override
             public void onFailure(Call<ApiData> call, Throwable t) {
                 if(t.getLocalizedMessage().equalsIgnoreCase("Unable to resolve host \"api.alquran.cloud\": No address associated with hostname"))
                     Toast.makeText(MainActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
                 else
                 Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                 Log.d("Error Arabiyah ", t.getMessage());
             }
         });

     }

    private void getEnglishTextData(QuranApi quranApi) {
        Call<ApiData> call = quranApi.getAyatEnglishText(mUserData.getAyat(),"en.asad");

        call.enqueue(new Callback<ApiData>() {
            @Override
            public void onResponse(Call<ApiData> call, Response<ApiData> response) {

                ApiData englishTextData;

                englishTextData = response.body();

                JsonObject jsonObject = englishTextData.data;

               String text = jsonObject.get("text").toString();

                Log.d("AYAT TRANSLATION ", text);

                mAyatEnglishTV.setText(text);

//                mAyatEnglishTV.setText(text);

                JsonObject jsonObject1 = jsonObject.getAsJsonObject("surah");

                String surahName = jsonObject1.get("englishName").toString();

                String[] surahNameSplit = surahName.split("\"");

                Log.d("SURAH NAME",surahNameSplit[1]);

                String ayatId = "Holy Qur\'an: "+surahNameSplit[1]+" ("+mUserData.getAyat()+")";

                mAyatNameTV.setText(ayatId);

                mFileImageName = surahNameSplit[1]+"_"+mUserData.getAyat();



            }

            @Override
            public void onFailure(Call<ApiData> call, Throwable t) {
                if(t.getLocalizedMessage().equalsIgnoreCase("Unable to resolve host \"api.alquran.cloud\": No address associated with hostname"))
                    Toast.makeText(MainActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
                else
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Error ", t.getMessage());
            }
        });
    }

    private void getAudioData(QuranApi quranApi){
         Call<ApiData> call = quranApi.getAudioData(mUserData.getAyat(),"ar.alafasy");

         call.enqueue(new Callback<ApiData>() {
             @Override
             public void onResponse(Call<ApiData> call, Response<ApiData> response) {

                 ApiData audioData;

                 audioData = response.body();

                 JsonObject jsonObject = audioData.data;

                 String text = jsonObject.get("audioSecondary").toString();

                 String[] data = text.split("\"");

                 mAudioLink = data[1];


                 Log.d("AYAT AUDIO", data[1]);

             }

             @Override
             public void onFailure(Call<ApiData> call, Throwable t) {
                 if(t.getLocalizedMessage().equalsIgnoreCase("Unable to resolve host \"api.alquran.cloud\": No address associated with hostname"))
                     Toast.makeText(MainActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
                 else
                 Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                 Log.d("Error ", t.getMessage());
             }
         });
    }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_main, menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {

         int id = item.getItemId();

         switch (id){

             case R.id.play_audio:
                 mMenuItem = item;
                 if(mAudioLink.equalsIgnoreCase("")) {
                     Toast.makeText(this, "Choose Ayat", Toast.LENGTH_SHORT).show();
                 }else{
                     if (!setStopIcon) {
                         mMenuItem.setIcon(R.drawable.ic_stop);
                         setStopIcon = true;
                     } else {
                         mMenuItem.setIcon(R.drawable.ic_play);
                     }

                     if (mMediaPlayer.isPlaying()) {
                         initialStage = true;
                         playPause = false;
//                         btn.setText("Launch Streaming");
                         mMediaPlayer.stop();
                         mMediaPlayer.reset();
                         setStopIcon = false;
                     } else {
                         playAudio();
                     }
                 }
         }

         return super.onOptionsItemSelected(item);
     }

     private void playAudio() {
         dialog = new AlertDialog.Builder(MainActivity.this);
         LayoutInflater inflater = getLayoutInflater();
         final View dialogView = inflater.inflate(R.layout.audio_player,null);
         dialog.setView(dialogView);



         if (!playPause) {

             if (initialStage) {
                 new Player().execute(mAudioLink);
             } else {
                 if (!mMediaPlayer.isPlaying())
                     mMediaPlayer.start();
             }

             playPause = true;

         } else {


             if (mMediaPlayer.isPlaying()) {
                 mMediaPlayer.pause();
             }

             playPause = false;
         }

     }

     class Player extends AsyncTask<String, Void, Boolean> {
         @Override
         protected Boolean doInBackground(String... strings) {
             Boolean prepared = false;

             try {
                 mMediaPlayer.setDataSource(strings[0]);
                 mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mediaPlayer) {
                         initialStage = true;
                         playPause = false;
//                         btn.setText("Launch Streaming");
                         mediaPlayer.stop();
                         mediaPlayer.reset();
                         mMenuItem.setIcon(R.drawable.ic_play);
                         setStopIcon = false;
                     }
                 });

                 mMediaPlayer.prepare();
                 prepared = true;

             } catch (Exception e) {
                 Log.e("MyAudioStreamingApp", e.getMessage());
                 prepared = false;
             }

             return prepared;
         }

         @Override
         protected void onPostExecute(Boolean aBoolean) {
             super.onPostExecute(aBoolean);

             if (progressDialog.isShowing()) {
                 progressDialog.cancel();
             }

             mMediaPlayer.start();
             initialStage = false;
         }

         @Override
         protected void onPreExecute() {
             super.onPreExecute();

             progressDialog.setMessage("Buffering...");
             progressDialog.show();
         }


     }
     public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url){
         DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
         Uri uri = Uri.parse(url);
         DownloadManager.Request request = new DownloadManager.Request(uri);

//             request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
         request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);


         downloadManager.enqueue(request);
     }
 }