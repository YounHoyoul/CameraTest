package com.yhy.cameratest;

import java.io.File;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class CameraImageUtil {
	private static final String TAG = "CAMERA::Activity";
	
	public synchronized static int GetExifOrientation(String filepath) {
	    int degree = 0;
	    ExifInterface exif = null;
	    
	    try {
	        exif = new ExifInterface(filepath);
	    } 
	    catch (IOException e) {
	        Log.e(TAG, "cannot read exif");
	        e.printStackTrace();
	    }
	    
	    if (exif != null) {
	        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
	        
	        if (orientation != -1){
	            // We only recognize a subset of orientation tag values.
	            switch(orientation){
	                case ExifInterface.ORIENTATION_ROTATE_90:
	                    degree = 90;
	                    break;
	                    
	                case ExifInterface.ORIENTATION_ROTATE_180:
	                    degree = 180;
	                    break;
	                    
	                case ExifInterface.ORIENTATION_ROTATE_270:
	                    degree = 270;
	                    break;
	            }
	        }
	    }
	    
	    return degree;
	}
	
	public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees){
	    if ( degrees != 0 && bitmap != null ) {
	        Matrix m = new Matrix();
	        m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2 );
	        try {
	            Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
	            if (bitmap != b2){
	            	bitmap.recycle();
	            	bitmap = b2;
	            }
	        } 
	        catch (OutOfMemoryError ex){
	            // We have no memory to rotate. Return the original bitmap.
	        }
	    }
	    
	    return bitmap;
	}
	
	public synchronized static Bitmap SafeDecodeBitmapFile(String strFilePath){
		//DEBUG.SHOW_DEBUG(TAG, "[ImageDownloader] SafeDecodeBitmapFile : " + strFilePath);
		
		Log.v(TAG, "[ImageDownloader] SafeDecodeBitmapFile : " + strFilePath);
		
		try{
	    	File file = new File(strFilePath);
	    	if (file.exists() == false){
	    		//DEBUG.SHOW_ERROR(TAG, "[ImageDownloader] SafeDecodeBitmapFile : File does not exist !!");
	    		
	    		Log.e(TAG, "[ImageDownloader] SafeDecodeBitmapFile : File does not exist !!");
	    		
	    		return null;
	    	}
	    	
	    	// Max image size
	    	//final int IMAGE_MAX_SIZE 	= GlobalConstants.getMaxImagePixelSize();	
	    	final int IMAGE_MAX_SIZE 	= 300;
	    	BitmapFactory.Options bfo 	= new BitmapFactory.Options();
	    	bfo.inJustDecodeBounds 		= true;
	    	
			BitmapFactory.decodeFile(strFilePath, bfo);
	        
	        if(bfo.outHeight * bfo.outWidth >= IMAGE_MAX_SIZE * IMAGE_MAX_SIZE){
	        	bfo.inSampleSize = (int)Math.pow(2, (int)Math.round(Math.log(IMAGE_MAX_SIZE 
	        						/ (double) Math.max(bfo.outHeight, bfo.outWidth)) / Math.log(0.5)));
	        }
	        bfo.inJustDecodeBounds = false;
	        bfo.inPurgeable = true;
	        bfo.inDither = true;
	        
	        final Bitmap bitmap = BitmapFactory.decodeFile(strFilePath, bfo);
	    	
	        int degree = GetExifOrientation(strFilePath);
	        
	    	return GetRotatedBitmap(bitmap, degree);
		}
		catch(OutOfMemoryError ex){
			ex.printStackTrace();
			return null;
		}
	}
}
