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

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * AsyncTask for display Jpeg frame on android
 *
 * @author nguyenquockhai (nqkhai1706@gmail.com) create on 16/07/2015 at Robotics Club.
 *
 */
public class FrameDisplay extends AsyncTask<Void, Void, Bitmap>
{
    private final WeakReference<ImageView> imageViewReference;
    private final Bitmap bitmap;


    public FrameDisplay
            (ImageView imageView, Bitmap bmp) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        bitmap = bmp;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Void... params) {
        return bitmap;
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
}
