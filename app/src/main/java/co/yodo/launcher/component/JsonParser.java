package co.yodo.launcher.component;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import co.yodo.launcher.helper.AppUtils;

/**
 * Created by luis on 22/01/15.
 * Parser for json files
 */
public class JsonParser {
    /** DEBUG */
    private static final String TAG = JsonParser.class.getSimpleName();

    /** Folder for files */
    private File cacheDir;

    /** Timeout */
    private final static int TIMEOUT = 10000;

    public JsonParser(Context context) {
        //Find the dir to save cached images
        if( android.os.Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ) )
            cacheDir = new File( android.os.Environment.getExternalStorageDirectory(), "LazyList" );
        else
            cacheDir = context.getCacheDir();
        if( !cacheDir.exists() )
            cacheDir.mkdirs();
    }

    public JSONArray getJSONFromUrl(String url) {
        File file = getFile();
        JSONArray jObj = getJSONArrayFromFile( file );
        if( jObj != null ) {
            return jObj;
        }

        if( file.exists() )
            file.delete();

        // Try to get json array
        try {
            URL imageUrl = new URL( url );
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout( TIMEOUT );
            conn.setReadTimeout( TIMEOUT );
            conn.setInstanceFollowRedirects( true );
            conn.connect();
            InputStream is  = conn.getInputStream();
            OutputStream os = new FileOutputStream( file );
            CopyStream( is, os );
            os.close();

            jObj = getJSONArrayFromFile( file );
        } catch(IOException e) {
            e.printStackTrace();
        }
        // return JSON Array
        return jObj;
    }

    private File getFile() {
        return new File( cacheDir, "currencies.json" );
    }

    public static boolean deleteFile(Context context) {
        File dir;
        if( android.os.Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ) )
            dir = new File( android.os.Environment.getExternalStorageDirectory(), "LazyList" );
        else
            dir = context.getCacheDir();

        File file = new File( dir, "currencies.json" );
        return file.delete();
    }

    private JSONArray getJSONArrayFromFile(File file) {
        if( !file.exists() )
            return null;

        try {
            FileInputStream fin   = new FileInputStream( file );
            BufferedReader reader = new BufferedReader( new InputStreamReader( fin ) );
            StringBuilder sb      = new StringBuilder();
            String line;

            while( ( line = reader.readLine() ) != null )
                sb.append( line ).append("\n");

            reader.close();
            fin.close();
            return new JSONArray( sb.toString() );
        } catch ( IOException | JSONException e ) {
            e.printStackTrace();
        }

        return null;
    }

    private static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for(;;) {
                int count = is.read( bytes, 0, buffer_size );
                if( count == -1 )
                    break;
                os.write( bytes, 0, count );
            }
        } catch(Exception ex) {
            AppUtils.Logger( TAG, ex.toString() );
        }
    }
}
