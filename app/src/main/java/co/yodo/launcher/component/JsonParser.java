package co.yodo.launcher.component;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by luis on 22/01/15.
 * Parser for json files
 */
public class JsonParser {
    public JSONArray getJSONFromUrl(String url) {
        JSONArray jObj = null;
        StringBuilder builder = new StringBuilder();
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost( url );
        // Try to get json array
        try {
            HttpResponse httpResponse = httpClient.execute( httpPost );
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream content = httpEntity.getContent();
            BufferedReader reader = new BufferedReader( new InputStreamReader( content ) );
            String line;
            while( ( line = reader.readLine() ) != null ) {
                builder.append( line );
            }
            jObj = new JSONArray( builder.toString() );
        } catch(IOException | JSONException e) {
            e.printStackTrace();
        }
        // return JSON Array
        return jObj;
    }
}
