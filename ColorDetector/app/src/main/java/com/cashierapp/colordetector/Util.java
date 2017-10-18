package com.cashierapp.colordetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by takuyamorimatsu on 2017/09/26.
 */

public class Util {

    public static String getTimeStamp(String format){

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.JAPAN);
        return sdf.format(date);

    }


    public static void writeMsg(Context context, String msg, String filename){
        /*BufferedWriter bufferedWriter = null;
        OutputStream out;

        try{
            out = context.openFileOutput(filename, Context.MODE_APPEND);
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
            printWriter.append(msg);
            printWriter.close();
            //Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();*/

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) { //マウントされているか

            String mydirName = "ColorData";
            File myDir = new File(Environment.getExternalStorageDirectory(), mydirName);
            if (!myDir.exists()) { //MyDirectoryというディレクトリーがなかったら作成
                myDir.mkdirs();
            }


            File saveFile = new File(myDir, filename);
            try {
                FileOutputStream outputStream = new FileOutputStream(saveFile, true);
                outputStream.write(msg.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { //読み取りのみか（書き込み不可）

            Toast.makeText(context, "書き込み不可", Toast.LENGTH_SHORT).show();
        }
        /*} catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
    }


    public static void writeErrorMsg(Context context, String msg, String filename){


        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) { //マウントされているか

            String mydirName = "ColorData_Error";
            File myDir = new File(Environment.getExternalStorageDirectory(), mydirName);
            if (!myDir.exists()) { //MyDirectoryというディレクトリーがなかったら作成
                myDir.mkdirs();
            }


            File saveFile = new File(myDir, filename);
            try {
                FileOutputStream outputStream = new FileOutputStream(saveFile, true);
                outputStream.write(msg.getBytes());
                outputStream.close();
                Toast.makeText(context, "多分書き込めました", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }



        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { //読み取りのみか（書き込み不可）

            Toast.makeText(context, "書き込み不可", Toast.LENGTH_SHORT).show();
        }

    }



    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }



    /*public static String readFile(Context context, String filename){
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;

        try{
            reader = new BufferedReader(new InputStreamReader(context.openFileInput(filename)));
            String line;
            int i = 0;
            while((line = reader.readLine()) != null && i < 1000){
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(reader != null){
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        Log.d("readFile", "6");

        return builder.toString();
    }*/


    public static int[] getPixelGBR(Bitmap bitmap, int x, int y){
        int color = bitmap.getPixel(x, y);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int[] rgb = {red, green, blue, color};
        return rgb;
    }


    public static int colorChecker(int red, int green, int blue, int border){

        //TODO:黒と白の境界を調整
        if(red < border && green < border && blue < border){
            //黒と判定
            return 0;
        }else{
            //白と判定
            return 255;
        }
    }


}
