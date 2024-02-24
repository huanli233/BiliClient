package com.RobinNotBad.BiliClient.api;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

class QRCodeGenerator {

    /**
     * 保存二维码到本地文件
     * @author air
     * @param url 二维码的内容
     * @param file 文件位置和名字
     */
    private void saveQRCode(String url, String file) {
        try {
            // 使用MultiFormatWriter将url编码成BitMatrix对象
            BitMatrix bitMatrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 300, 300);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            // 创建位图对象
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            // 遍历BitMatrix对象中的每个像素点，将对应位置的颜色设置到位图对象中
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // 如果当前位置的bit值为真（黑色），则设置为Color.BLACK；否则设置为Color.WHITE（白色）
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            // 创建文件对象并获取文件输出流
            File qrCodeFile = new File(file);
            FileOutputStream outputStream = new FileOutputStream(qrCodeFile);

            // 将位图对象压缩成PNG格式并保存到文件中
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            outputStream.close();
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }
}