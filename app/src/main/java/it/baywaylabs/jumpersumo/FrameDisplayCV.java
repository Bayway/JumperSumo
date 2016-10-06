/*
 * Copyright (C) 2015 Massimiliano Fiori [massimiliano.fiori@aol.it].
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.baywaylabs.jumpersumo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARDeviceController;

import java.lang.ref.WeakReference;


import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * AsyncTask for display Jpeg frame on android and find qr-code with OpenCV library.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 *
 */
public class FrameDisplayCV extends AsyncTask<Void, Void, Bitmap>
{
    private final WeakReference<ImageView> imageViewReference;
    private final Bitmap bitmapOriginal;
    private final ARDeviceController deviceController;
    //private ImageView imageView;
    private Boolean flag_execute_qrcode;
    private Mat imgMAT;
    //private Bitmap bitmapTranform;

    private static final String TAG = FrameDisplayCV.class.getSimpleName();

    /**
     *
     * @param imageView ImageView where show frame.
     * @param bmp Bitmap frame..
     * @param deviceController ARDeviceController.
     * @param flag true if want execute Jump command.
     */
    public FrameDisplayCV(ImageView imageView, Bitmap bmp, ARDeviceController deviceController, Boolean flag) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        this.imageViewReference = new WeakReference<ImageView>(imageView);
        //this.imageView = imageView;
        this.bitmapOriginal = bmp;
        this.deviceController = deviceController;
        this.flag_execute_qrcode = flag;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Void... params) {

        if(bitmapOriginal != null)
        {
            this.imgMAT = new Mat (bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), CvType.CV_8UC4);
            try {
                zxing();
            } catch (ChecksumException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }
            Bitmap bitmapTranform = Bitmap.createBitmap(this.imgMAT.width(), this.imgMAT.height(), Bitmap.Config.ARGB_8888);
            //bitmapOriginal = Bitmap.createBitmap(imgMAT.width(), imgMAT.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(this.imgMAT, bitmapTranform);

            // return bitmapTranform;
        }

        return bitmapOriginal;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bmp) {
        if (bmp != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bmp);
            }
        }
    }

    /**
     * This method find a qr-code in the view cam and execute some control.
     *
     * @throws ChecksumException
     * @throws FormatException
     */
    private void zxing() throws ChecksumException, FormatException {

        int[] intArray = new int[bitmapOriginal.getWidth()*bitmapOriginal.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bitmapOriginal.getPixels(intArray, 0, bitmapOriginal.getWidth(), 0, 0, bitmapOriginal.getWidth(), bitmapOriginal.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(),intArray);

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new QRCodeMultiReader();

        String sResult = "";
        Double AREA_RIFERIMENTO = 11500.0;

        try {

            Result result = reader.decode(bitmap);
            sResult = result.getText();
            if(result.getBarcodeFormat().compareTo(BarcodeFormat.QR_CODE) == 0) {

                Log.d(TAG, "SI! E' Un QRCode");
                if ("jump".equalsIgnoreCase(sResult) && this.deviceController != null && this.flag_execute_qrcode)
                {
                    deviceController.getFeatureJumpingSumo().sendAnimationsJump(ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_HIGH);
                }
            }

            ResultPoint[] points = result.getResultPoints();
            Log.d(TAG, "PUNTI: " + points.toString());

            Point a = new Point(points[0].getX(), points[0].getY());
            Point b = new Point(points[2].getX(), points[2].getY());
            Rect rect = new Rect(a, b);

            Log.d(TAG, "Area del rettangolo: " + rect.area());
            if( rect.area() < AREA_RIFERIMENTO)
                Log.w(TAG, "Mi devo avvicinare!");
            else
                Log.w(TAG, "Mi devo allontanare!");

            Imgproc.rectangle(this.imgMAT, new Point(points[0].getX(), points[0].getY()), new Point(points[2].getX(), points[2].getY()), new Scalar(0, 255, 0), 3);
            Log.d(TAG, sResult);
            Point center = new Point(0, 0);

            Imgproc.circle(this.imgMAT, center, 10, new Scalar(0, 0, 255),2);
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Code Not Found");
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

    }
}