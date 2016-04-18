package co.yodo.launcher.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.volley.toolbox.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by hei on 18/04/16.
 * cache in memory
 */
public class MemoryBMCache implements ImageLoader.ImageCache {
    private File cacheDir;

    public MemoryBMCache( Context ac ) {
        cacheDir = ac.getCacheDir();
    }

    @Override
    public Bitmap getBitmap( String url ) {
        //return get(url);
        String filename = String.valueOf( url.hashCode() );
        File file = new File( cacheDir, filename );
        //return decodeFile( file );
        FileInputStream fis = null;
        try {
            fis = new FileInputStream( file );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream( fis );
    }

    @Override
    public void putBitmap( String url, Bitmap bitmap ) {
        //put(url, bitmap);
        String filename = String.valueOf( url.hashCode() );
        File file = new File( cacheDir, filename );

        try {
            FileOutputStream out = new FileOutputStream( file );
            bitmap.compress( Bitmap.CompressFormat.PNG, 100, out );
            out.flush();
            out.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if( files == null )
            return;
        for( File f : files )
            f.delete();
    }
}