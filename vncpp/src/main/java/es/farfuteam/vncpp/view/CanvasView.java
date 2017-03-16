/*
 	Copyright 2013 Oscar Crespo Salazar
 	Copyright 2013 Gorka Jimeno Garrachon
 	Copyright 2013 Luis Valero Martin
  
	This file is part of VNCpp.

	VNCpp is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	any later version.
	
	VNCpp is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with VNCpp.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.farfuteam.vncpp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @class CanvasView
 * @brief This is the class which controls the bitmap.
 * <p>
 * This is the detailed description
 * @authors Oscar Crespo, Gorka Jimeno, Luis Valero
 * @extends ImageView
 */
public class CanvasView extends ImageView {

    /**
     * The bitmap data
     */
    private int[] data;
    /**
     * The image height
     */
    private int height;
    /**
     * The image width
     */
    private int width;
    /**
     * The stride of bitmap
     */
    private int stride;
    /**
     * The x coordinate
     */
    private int x;
    /**
     * The y coordinate
     */
    private int y;
    /**
     * The offset bitmap
     */
    private int offset;
    /**
     * The bitmap
     */
    private Bitmap bitmap;
    /**
     * The image drag
     */
    private boolean drag = false;
    /**
     * The image update
     */
    private boolean update = false;
    /**
     * The auxiliary bitmap
     */
    private Rect bitmapRect;
    /**
     * Scale factor
     */
    private float scale = 1;
    /**
     * The X scale factor
     */
    private float scaleX = 0;
    /**
     * The Y scale factor
     */
    private float scaleY = 0;

    /**
     * @param context
     * @param attrs
     * @brief Constructor method
     * @details Constructor method
     */
    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        data = null;
    }

    /**
     * @param canvas
     * @brief Override onDraw method
     * @details Called when the canvas is drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (data != null) {
            canvas.save();
            canvas.scale(scale, scale, scaleX, scaleY);
            if (!drag) {
                canvas.drawBitmap(data, offset, stride, x, y, width, height, false, null);
            } else {

                canvas.drawBitmap(bitmap, bitmapRect, bitmapRect, null);

            }

        }

    }

    /**
     * @return height
     * @brief Returns the height attribute
     * @details Returns the height attribute
     */
    public int getRealHeight() {
        return height;
    }

    /**
     * @return width
     * @brief Returns the width attribute
     * @details Returns the width attribute
     */
    public int getRealWidth() {
        return width;
    }

    /**
     * @return scale
     * @brief Returns the scale attribute
     * @details Returns the scale attribute
     */
    public float getScale() {
        return scale;
    }

    /**
     * @param scale
     * @param scaleX
     * @param scaleY
     * @brief Sets the scales attributes
     * @details Sets the scales attributes
     */
    public void setScale(float scale, float scaleX, float scaleY) {
        this.scale = scale;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /**
     * @brief Creates a temporal bitmap
     * @details Creates a temporal bitmap when the user moves around the image
     */
    public void startDrag() {
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(data, offset, stride, width, height, Bitmap.Config.RGB_565);
        }
        update = false;
        drag = true;
    }

    /**
     * @brief Destroys the temporal bitmap
     * @details Invalidates the temporal bitmap created on startDrag()
     */
    public void endDrag() {
        drag = false;
        if (update) {
            bitmap = null;
        }
        postInvalidate();
    }

    /**
     * @brief Updates the bitmap
     * @details Updates the bitmap
     */
    public void reDraw() {
        if (!drag) {
            bitmap = null;

        } else {
            update = true;
        }

    }

    /**
     * @param data   Bitmap data
     * @param offset The offset is the array position with the first pixel
     * @param stride The array stride
     * @param x      The X coordinate
     * @param y      The Y coordinate
     * @param width  The width
     * @param height The height
     * @brief Initialized Canvas
     * @details Initialized Canvas with the indicated parameters
     */
    public void initCanvas(int[] data, int offset, int stride, int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.data = data;
        this.width = width;
        this.height = height;
        this.stride = stride;
        this.offset = offset;

        bitmapRect = new Rect(0, 0, width, height);


    }
}
