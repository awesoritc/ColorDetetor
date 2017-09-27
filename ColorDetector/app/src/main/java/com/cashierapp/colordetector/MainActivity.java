package com.cashierapp.colordetector;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    //TODO:設定から戻ってきた時のcameraのエラーを削除
    //Camera is being used after Camera.release() was called



    /*

        //TODO:各設定値を指定


    //TODO:filenameを指定
    private final String filename = "filename";

    //TODO:白黒の境界値を指定(0~255)
    //RGB全てが100以下であるなら黒と判別
    private final int border = 100;

    //TODO:シャッター間隔を指定(ミリ秒)
    //1秒
    private final int interval = 1000;

    //TODO:(1箇所だけの色をとる場合)色をとる位置(横:右から左に0~2559, 縦:上から下に0~1919)
    //右上
    private final int x_pos = 100;
    private final int y_pos = 100;

    //TODO:モード切り替え(5箇所の色の多い結果を使う場合はtrue, 1箇所の色を使うならfalse)
    //(5箇所)角の4箇所と真ん中の色を取得し、多い方の色を記録
    //(1箇所)上の設定値の位置の色を取得
    //白黒判定は上で指定した境界値に従う
    private final boolean use5points = false;

    //TODO:時間のフォーマットを指定
    private final String format = "yyyy/MM/dd HH:mm:ss.SSS";

    */

    private String filename;
    private int border;
    private int interval;
    private int x_pos;
    private int y_pos;
    private boolean use5points;
    private String format;

    public void setValues(){
        SharedPreferences preferences = getSharedPreferences("setting", MODE_PRIVATE);
        filename = preferences.getString("filename_input", "filename.txt");
        border = preferences.getInt("border_input", 100);
        interval = preferences.getInt("interval_input", 1000);
        x_pos = preferences.getInt("x_pos_input", 100);
        y_pos = preferences.getInt("y_pos_input", 100);
        format = preferences.getString("format_input", "yyyy/MM/dd HH:mm:ss.SSS");

        use5points = preferences.getBoolean("use5points_input", false);
    }





    // カメラインスタンス
    private Camera mCam = null;
    private Bitmap pic;
    private TextView palette, result, recent_data;

    // カメラプレビュークラス
    private CameraPreview mCamPreview = null;

    Handler mHandler;
    private boolean isRunning = false;
    private Button delete_btn, display_file_btn, setting_btn;
    private ScrollView scroll;
    FrameLayout preview;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        palette = (TextView) findViewById(R.id.color);
        result = (TextView) findViewById(R.id.result);
        recent_data = (TextView) findViewById(R.id.recent_data);
        scroll = (ScrollView) findViewById(R.id.scrollView);
        preview = (FrameLayout)findViewById(R.id.preview);

        setValues();

        final Button btn = (Button) findViewById(R.id.start_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRunning){
                    /*if (mCam != null) {
                        mCam.release();
                        mCam = null;
                    }*/

                    if(mHandler != null){
                        mHandler.removeCallbacksAndMessages(null);
                    }

                    isRunning = false;
                    btn.setText("start");

                    /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("記録したデータを確認しますか");
                    builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            scroll.setVisibility(View.VISIBLE);
                            result.setText(Util.readFile(MainActivity.this, filename));
                        }
                    });
                    builder.setNegativeButton("いいえ", null);
                    builder.create().show();*/
                    //止まってる時のもの
                    recent_data.setVisibility(View.GONE);
                    //display_file_btn.setVisibility(View.VISIBLE);
                    preview.setVisibility(View.GONE);
                    setting_btn.setVisibility(View.VISIBLE);
                }else{

                    try {
                        mCam = Camera.open();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    // FrameLayout に CameraPreview クラスを設定
                    preview = (FrameLayout)findViewById(R.id.preview);
                    preview.setVisibility(View.VISIBLE);
                    mCamPreview = new CameraPreview(MainActivity.this, mCam);
                    preview.addView(mCamPreview);


                    mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mCam.takePicture(null, null, mPicJpgListener);
                            mHandler.postDelayed(this, interval);
                        }
                    }, 0);
                    isRunning = true;
                    btn.setText("stop");
                    //動いている時のもの
                    delete_btn.setVisibility(View.GONE);
                    recent_data.setVisibility(View.VISIBLE);
                    scroll.setVisibility(View.GONE);
                    //display_file_btn.setVisibility(View.GONE);
                    setting_btn.setVisibility(View.GONE);
                }
            }
        });

        /*display_file_btn = (Button) findViewById(R.id.display_file_btn);
        display_file_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scroll.setVisibility(View.VISIBLE);
                result.setText(Util.readFile(MainActivity.this, filename));
                display_file_btn.setVisibility(View.GONE);
                delete_btn.setVisibility(View.VISIBLE);
            }
        });*/


        delete_btn = (Button) findViewById(R.id.delete_btn);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder confirm = new AlertDialog.Builder(MainActivity.this);
                confirm.setMessage("本当に削除してよろしいですか？");
                confirm.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteFile(filename);
                        Toast.makeText(MainActivity.this, "削除しました", Toast.LENGTH_SHORT).show();
                        scroll.setVisibility(View.GONE);
                        delete_btn.setVisibility(View.GONE);
                    }
                });
                confirm.setNegativeButton("いいえ", null);
                confirm.create().show();
            }
        });


        setting_btn = (Button) findViewById(R.id.setting_btn);
        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(intent, 0);
            }
        });


        // カメラインスタンスの取得
        /*try {
            mCam = Camera.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        // FrameLayout に CameraPreview クラスを設定
        FrameLayout preview = (FrameLayout)findViewById(R.id.image);
        mCamPreview = new CameraPreview(this, mCam);
        preview.addView(mCamPreview);*/
    }

    public void initializeCamera(){
        try {
            mCam = Camera.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        mCamPreview = new CameraPreview(this, mCam);

        preview.addView(mCamPreview);
    }


    /**
     * JPEG データ生成完了時のコールバック
     */
    private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }

            pic = BitmapFactory.decodeByteArray(data, 0, data.length);//width:2560, height:1920
            /*int color = pic.getPixel(100, 100);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);*/

            //int[] rgb = Util.getPixelGBR(pic, 100, 100);

            String tmp = "";
            int selected_color = 0;
            if(use5points){
                //5箇所をとるパターン
                int[] points_width = {100, 100, 2460, 2460, 1230};
                int[] points_height = {100, 1820, 100, 1820, 960};


                int black_counter = 0;
                for(int i = 0; i < 5; i++){
                    int[] rgb = Util.getPixelGBR(pic, points_width[i], points_height[i]);
                    if(Util.colorChecker(rgb[0], rgb[1], rgb[2], border) == 0){
                        black_counter++;
                    }
                    selected_color = rgb[3];

                }
                if(black_counter > 3){
                    tmp = "0," + Util.getTimeStamp(format);
                }else{
                    tmp = "255," + Util.getTimeStamp(format);
                }
            }else{
                //右上の１箇所だけのパターン
                int[] rgb = Util.getPixelGBR(pic, x_pos, y_pos);
                tmp = Util.colorChecker(rgb[0], rgb[1], rgb[2], border) + "," + Util.getTimeStamp(format);
                selected_color = rgb[3];
            }


            //nullになりうる？


            //Util.appendLog(MainActivity.this, "test1.txt", String.valueOf(color) + "\n");
            //Toast.makeText(MainActivity.this, String.valueOf(color) + "\n" + Util.getTimeStamp(), Toast.LENGTH_SHORT).show();
            ///Toast.makeText(MainActivity.this, String.valueOf(red) + "\n" + String.valueOf(green) + "\n" + String.valueOf(blue), Toast.LENGTH_SHORT).show();
            //String tmp = String.valueOf(Util.colorChecker(rgb[0], rgb[1], rgb[2], border)) + "," + Util.getTimeStamp();
            //Toast.makeText(MainActivity.this, String.valueOf(pic.getWidth()) + "\n" + String.valueOf(pic.getHeight()), Toast.LENGTH_SHORT).show();
            //Toast.makeText(MainActivity.this, tmp, Toast.LENGTH_SHORT).show();
            recent_data.setText(tmp);
            Util.writeMsg(MainActivity.this, tmp +"\n", filename);
            palette.setBackgroundColor(selected_color);



            // takePicture するとプレビューが停止するので、再度プレビュースタート
            mCam.startPreview();
        }
    };



    @Override
    protected void onPause() {
        super.onPause();
        // カメラ破棄インスタンスを解放
        if (mCam != null) {
            mCam.release();
            mCam = null;
        }

        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }

        isRunning = false;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //変更した設定値を読み込み
        setValues();

        delete_btn.setVisibility(View.GONE);
    }




}

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCam;

    /**
     * コンストラクタ
     */
    public CameraPreview(Context context, Camera cam) {
        super(context);

        mCam = cam;

        // サーフェスホルダーの取得とコールバック通知先の設定
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * SurfaceView 生成
     */
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // カメラインスタンスに、画像表示先を設定
            mCam.setPreviewDisplay(holder);
            mCam.setDisplayOrientation(90);
            // プレビュー開始
            mCam.startPreview();
        } catch (IOException e) {
            //
        }
    }

    /**
     * SurfaceView 破棄
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    /**
     * SurfaceHolder が変化したときのイベント
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 画面回転に対応する場合は、ここでプレビューを停止し、
        // 回転による処理を実施、再度プレビューを開始する。
    }



}
