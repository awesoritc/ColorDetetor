package com.cashierapp.colordetector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final String ERROR_INSTANCE = "e";

    //StringBuilderを使ってまとめてからファイルに書き出し
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

    //TODO:(1箇所だけの色をとる場合)色をとる位置(縦:上から下に0~2559, 横:右から左に0~1919)
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
    private int x_pos, x_area;
    private int y_pos, y_area;
    //use5points:5点での色判断を行う場合にtrue
    //printRGB:RGBの値をログに出力する場合にtrue
    private boolean use5points, printRGB, randomPoints;
    private String format;
    private String current_rgb_log = "";

    public void setValues(){
        SharedPreferences preferences = getSharedPreferences("setting", MODE_PRIVATE);
        //filename = preferences.getString("filename_input", Util.getTimeStamp("yyyy/MM/dd HH:mm") + ".txt");
        filename = Util.getTimeStamp("yyyy:MM:dd_HH:mm") + ".txt";
        border = preferences.getInt("border_input", 100);
        interval = preferences.getInt("interval_input", 1000);
        x_pos = preferences.getInt("x_pos_input", 100);//(上から下に0~2560)
        y_pos = preferences.getInt("y_pos_input", 100);//(右から左に0~1920)
        x_area = 40;
        y_area = 40;
        format = preferences.getString("format_input", "yyyy/MM/dd HH:mm:ss.SSS");

        use5points = preferences.getBoolean("use5points_input", false);

        printRGB = preferences.getBoolean("printRGB_input", false);

        randomPoints = preferences.getBoolean("randomPoints_input", false);
    }





    // カメラインスタンス
    private Camera mCam = null;
    private Bitmap[] pic;
    private int pic_counter;
    private final int PIC_NUM = 3;
    private TextView palette, result, recent_data;

    // カメラプレビュークラス
    private CameraPreview mCamPreview = null;

    private Handler mHandler;
    private boolean isRunning = false;
    private Button setting_btn, error_btn;
    private FrameLayout preview;

    /*private StringBuilder builder;
    private int log_counter = 0;*/

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        palette = (TextView) findViewById(R.id.color);
        recent_data = (TextView) findViewById(R.id.recent_data);
        preview = (FrameLayout)findViewById(R.id.preview);
        pic = new Bitmap[PIC_NUM];
        pic_counter = 0;
        //LOGTAG
        /*builder = new StringBuilder();*/



        //予想しないExceptionを処理
        MyUncaughtExceptionHandler myUncaughtExceptionHandler = new MyUncaughtExceptionHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(myUncaughtExceptionHandler);


        setValues();

        final Button btn = (Button) findViewById(R.id.start_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRunning){
                    //止める

                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    if(mHandler != null){
                        mHandler.removeCallbacksAndMessages(null);
                    }

                    isRunning = false;
                    btn.setText("start");

                    //止まってる時のもの
                    recent_data.setVisibility(View.GONE);
                    preview.setVisibility(View.GONE);
                }else{
                    //動かす

                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    /*if(mCam != null){
                        try {
                            mCam = Camera.open();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }*/


                    // FrameLayout に CameraPreview クラスを設定
                    preview = (FrameLayout)findViewById(R.id.preview);
                    preview.setVisibility(View.VISIBLE);
                    mCamPreview = new CameraPreview(MainActivity.this, mCam);
                    preview.addView(mCamPreview);


                    mHandler = new Handler();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCam.takePicture(null, null, mPicJpgListener);
                            mHandler.postDelayed(this, interval);
                        }
                    });
                    isRunning = true;
                    btn.setText("stop");
                    //動いている時のもの
                    recent_data.setVisibility(View.VISIBLE);
                    setting_btn.setVisibility(View.GONE);
                    error_btn.setVisibility(View.GONE);
                }
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

        error_btn = (Button) findViewById(R.id.error_btn);
        final SharedPreferences pref = getSharedPreferences("error", MODE_PRIVATE);
        final String errormsg = pref.getString("error", ERROR_INSTANCE);
        String day = Util.getTimeStamp("yyyy:MM:dd_HH:mm");
        final String m = day + ".txt";
        if(!errormsg.equals(ERROR_INSTANCE)){
            error_btn.setVisibility(View.VISIBLE);
            error_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.writeErrorMsg(MainActivity.this, errormsg, m/*"error_msg.txt"*/);
                    pref.edit().putString("error", ERROR_INSTANCE).commit();
                    error_btn.setVisibility(View.GONE);
                }
            });
        }
    }




    @Override
    protected void onResume() {
        super.onResume();

        initializeCamera();
    }

    public void initializeCamera(){
        try {
            mCam = Camera.open();
            Camera.Parameters parameters = mCam.getParameters();
            if(parameters.isVideoStabilizationSupported()){
                parameters.setVideoStabilization(false);
            }

            //TODO:調子が悪ければオートフォーカスをonに
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);

            /*Camera.Parameters parameters = mCam.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCam.setParameters(parameters);*/

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


            pic[pic_counter%PIC_NUM] = BitmapFactory.decodeByteArray(data, 0, data.length);//width:2560, height:1920
            /*int color = pic.getPixel(100, 100);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);*/

            //int[] rgb = Util.getPixelGBR(pic, 100, 100);

            String tmp = "";
            int selected_color = 0;
            current_rgb_log = "";

            if(use5points){
                //5箇所をとるパターン

                //int[] points_width = {100, 100, 2460, 2460, 1230};
                //int[] points_height = {100, 1820, 100, 1820, 960};

                int[] points_width = {100, 100, 2460, 2460, 1230};
                int[] points_height = {100, 1820, 100, 1820, 960};

                if(randomPoints){
                    //範囲内でランダムな5箇所を取得

                    //b <= x < (b+a)
                    Random random = new Random();
                    for(int i = 0; i < 5; i++){
                        points_width[i] = random.nextInt(500/*取りうる範囲(a)*/) + 100;/*最小の値(b)*/
                    }
                    for(int i = 0; i < 5; i++){
                        points_height[i] = random.nextInt(500/*取りうる範囲(a)*/) + 100;/*最小の値(b)*/
                    }

                    //確認の出力をします。必要ないなら消してください(下のコメントまで)
                    String con_tmp = Util.getTimeStamp(format);
                    for (int i = 0; i < 5; i++) {
                        if(i != 4){
                            con_tmp += "(" + points_width[i] + ":" + points_height[i] + "),";
                        }else{
                            con_tmp += "(" + points_width[i] + ":" + points_height[i] + ")";
                        }

                    }
                    Util.writeMsg(MainActivity.this, con_tmp + "\n", "confirm.txt");
                    //ここまで
                }



                int black_counter = 0;
                for(int i = 0; i < 5; i++){
                    int[] rgb = Util.getPixelGBR(pic[pic_counter%PIC_NUM], points_width[i], points_height[i]);
                    if(i == 4){
                        current_rgb_log += "(" + String.valueOf(rgb[0]) + ":" +String.valueOf(rgb[1]) + ":" + String.valueOf(rgb[2]) + ")";//RGBの巡
                    }else{
                        current_rgb_log += "(" + String.valueOf(rgb[0]) + ":" +String.valueOf(rgb[1]) + ":" + String.valueOf(rgb[2]) + "),";//RGBの巡
                    }

                    if(Util.colorChecker(rgb[0], rgb[1], rgb[2], border) == 0){
                        black_counter++;
                    }
                    selected_color = rgb[3];

                }
                if(black_counter >= 3){
                    tmp = "0," + Util.getTimeStamp(format);
                }else{
                    tmp = "255," + Util.getTimeStamp(format);
                }
            }else{
                //右上の１箇所だけのパターン
                int[] rgb = Util.getPixelGBR(pic[pic_counter%PIC_NUM], x_pos, y_pos);
                current_rgb_log = "(" + String.valueOf(rgb[0]) + ":" +String.valueOf(rgb[1]) + ":" + String.valueOf(rgb[2]) + ")";//RGBの巡
                tmp = Util.colorChecker(rgb[0], rgb[1], rgb[2], border) + "," + Util.getTimeStamp(format);
                selected_color = rgb[3];
            }

            pic[pic_counter%PIC_NUM] = null;
            pic_counter++;


            //nullになりうる？

            recent_data.setText(tmp);
            Log.d(TAG, filename);

            //毎回出力せずにまとめてから出力する
            if(printRGB){
                //RGBの値をログに出力
                Util.writeMsg(MainActivity.this, tmp + "," + current_rgb_log + "\n", filename);

                /*ログの書き出しをまとめて行う
                //LOGTAG
                String a = tmp + "," + current_rgb_log + "\n";
                builder.append(a);*/
            }else{
                //普通のログを出力
                Util.writeMsg(MainActivity.this, tmp + "\n", filename);

                //LOGTAG
                /*String b = tmp + "\n";
                builder.append(b);*/
            }



            //LOGTAG
            /*if(log_counter > 100){
                if(printRGB){
                    //RGBの値をログに出力
                    Util.writeMsg(MainActivity.this, builder.toString(), filename);
                }else{
                    //普通のログを出力
                    Util.writeMsg(MainActivity.this, builder.toString(), filename);
                }
                builder.setLength(0);
                log_counter = 0;
            }else{
                log_counter++;
            }*/

            palette.setBackgroundColor(selected_color);



            // takePicture するとプレビューが停止するので、再度プレビュースタート
            mCam.startPreview();
        }
    };



    @Override
    protected void onPause() {
        super.onPause();

        //止めた時にログ出力される用にする
        //LOGTAG
        /*if(builder.length() != 0){
            if(printRGB){
                //RGBの値をログに出力
                Util.writeMsg(MainActivity.this, builder.toString(), filename);
            }else{
                //普通のログを出力
                Util.writeMsg(MainActivity.this, builder.toString(), filename);
            }
            builder.setLength(0);
            log_counter = 0;
        }*/


        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }

        isRunning = false;


        // カメラ破棄インスタンスを解放
        if (mCam != null) {
            mCam.release();
            mCam = null;
            finish();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //変更した設定値を読み込み
        setValues();
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
