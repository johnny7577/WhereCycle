package tw.com.jh7577.wherecycle.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtil {

	public static String get(String urlString) throws Exception {
		InputStream is = null;
		Reader reader = null;
		StringBuilder str = new StringBuilder();
		URL url = new URL(urlString);
		URLConnection URLConn = url.openConnection();
		URLConn.setRequestProperty("User-agent", "IE/6.0");
		is = URLConn.getInputStream();
		reader = new InputStreamReader(is, "UTF-8");
		char[] buffer = new char[1];
		while (reader.read(buffer) != -1) {
			str.append(new String(buffer));
		}
		return str.toString();
	}



}
