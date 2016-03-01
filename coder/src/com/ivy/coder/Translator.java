package com.ivy.coder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import com.ivy.coder.utils.StringUtils;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2015-1-8
 */
public class Translator {

    public static final String GOOGLE_TRANSLATE_PATH = "https://translate.google.com/translate_a/single?" +
    		"client=t&sl=zh-CN&tl=en&hl=zh-CN&dt=bd&dt=ex&dt=ld&dt=md&dt=qc&dt=rw&dt=rm&dt=ss&dt=t&dt=at&" +
    		"ie=UTF-8&oe=UTF-8&prev=btn&ssel=3&tsel=3&tk=517419|932453&q=";
    
    public static String httpRequest(String path, Map<String, String> properties) {
        HttpURLConnection httpURLConnection = null;
        BufferedReader br = null;
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.2.200.110", 8080));   
            URL url = new URL(path);
            httpURLConnection = (HttpURLConnection)url.openConnection(proxy);
            
            if (properties != null) {
                for (Entry<String, String> property : properties.entrySet()) {
                    httpURLConnection.addRequestProperty(property.getKey(), property.getValue());
                }
            }
            
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            httpURLConnection.connect();
            
            StringBuffer resultBuffer = new StringBuffer();
            br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                resultBuffer.append(line);            
            }
            
            return resultBuffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();    
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }  
        }
        
        return null;
    }
    
    @SuppressWarnings("deprecation")
    public static String translate(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        str = URLEncoder.encode(str);
        String url = GOOGLE_TRANSLATE_PATH + str;
        String result = httpRequest(url, null);
        int start = result.indexOf("\"");
        result = result.substring(start+1, result.indexOf("\"", start+1));
        return result;
    }
    
    public static void main(String[] args) {
        System.out.println(translate("我们是祖国的花朵"));
        /*Map<String, String> properties = new HashMap<String, String>();
        properties.put("Referer", "https://translate.google.com/");
        String result = httpRequest(url, properties);
        System.out.println(result);
        
        System.out.println();*/
    }
}
