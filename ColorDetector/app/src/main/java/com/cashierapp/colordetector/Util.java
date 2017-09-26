package com.cashierapp.colordetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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



    public static void appendLog(Context context, String filename, String contents){

        FileOutputStream out;
        File file = new File(context.getFilesDir(), filename);

        try{
            out = context.openFileOutput(filename, Context.MODE_WORLD_READABLE);
            out.write(contents.getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void writeMsg(Context context, String msg, String filename){
        BufferedWriter bufferedWriter = null;
        OutputStream out;

        try{
            out = context.openFileOutput(filename, Context.MODE_APPEND);
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
            printWriter.append(msg);
            printWriter.close();
            //Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public static String readFile(Context context, String filename){
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
    }


    public static int[] getPixelGBR(Bitmap bitmap, int x, int y){
        int color = bitmap.getPixel(x, y);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int[] rgb = {red, green, blue, color};
        return rgb;
    }


    public static int colorChecker(int red, int green, int blue, int border){

        //TODO:黒と城の境界を調整
        if(red < border && green < border && blue < border){
            //黒と判定
            return 0;
        }else{
            //白と判定
            return 255;
        }
    }
}
