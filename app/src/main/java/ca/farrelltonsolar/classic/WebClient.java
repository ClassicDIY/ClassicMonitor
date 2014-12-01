package ca.farrelltonsolar.classic;

import android.net.Uri;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

public class WebClient
{
	private WebClient()
	{
			
	}
	
	public static HttpClient getWebClient()
	{
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(new BasicHttpParams(), schemeRegistry);
        HttpParams connManagerParams = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(connManagerParams, Constants.MAX_TOTAL_CONNECTION);
        ConnManagerParams.setMaxConnectionsPerRoute(connManagerParams, new ConnPerRouteBean(Constants.MAX_CONNECTIONS_PER_ROUTE));
        HttpConnectionParams.setConnectionTimeout(connManagerParams, Constants.HTTP_CONNECT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(connManagerParams, Constants.HTTP_SO_TIMEOUT);
        HttpConnectionParams.setLinger(connManagerParams, 30);
		HttpClient httpclient = new DefaultHttpClient(cm, connManagerParams);
		try
		{
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            org.apache.http.conn.ssl.SSLSocketFactory sf = org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory();
            sf.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            schemeRegistry.register(new Scheme("https", sf, 443));
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		}
		catch(Exception e)
		{
            Log.d(Constants.LOG_TAG, ("Could not add SSL"));
            e.printStackTrace();
		}
		return httpclient;
		
	}
    public static final String VERSION = "1.4.7.1";

	public static HttpPost getPVOutputPost(String hostname, int port, String query, String sid, String key, String format, String data)
	{
		StringBuffer url = new StringBuffer();
		url.append("http://").append(hostname).append(":").append(port).append(query);
		HttpPost http = new HttpPost(url.toString());
		http.setHeader("X-Pvoutput-SystemId", sid);
		http.setHeader("X-Pvoutput-Apikey", key);
		if(format != null)
		{
			http.setHeader("User-Agent", "PVOutput/" + VERSION + " (SystemId:" + sid + "; Inverter:" + format + ")");
		}
		else
		{
			http.setHeader("User-Agent", "PVOutput/" + VERSION);
		}
		if(data != null)
		{
			try
			{
				StringEntity in = new StringEntity(data);
				http.setEntity(in);
			}
			catch(Exception e)
			{
				
			}
		}
		return http;
	}
	
	public static HttpGet getPVOutputGet(String hostname, int port, String query, String sid, String key, String format)
	{
		StringBuffer url = new StringBuffer();
		url.append("http://").append(hostname).append(":").append(port).append(query);
        Log.d(Constants.LOG_TAG, ">>> " + url.toString());
		HttpGet http = new HttpGet(url.toString());
		http.setHeader("X-Pvoutput-SystemId", sid);
		http.setHeader("X-Pvoutput-Apikey", key);
		if(format != null)
		{
			http.setHeader("User-Agent", "PVOutput/" + VERSION + " (SystemId:" + sid + "; Inverter:" + format + ")");
		}
		else
		{
			http.setHeader("User-Agent", "PVOutput/" + VERSION);
		}
		return http;
	}
	
	public static HttpGet getHttpGet(String hostname, int port, String query, boolean secure)
	{
		StringBuffer url = new StringBuffer();
		if(secure)
		{
			url.append("https://").append(hostname).append(":").append(port).append(query);
		}
		else
		{
			url.append("http://").append(hostname).append(":").append(port).append(query);
		}
        Log.d(Constants.LOG_TAG, ">>> " + url.toString());
		HttpGet http = new HttpGet(url.toString());		
		http.setHeader("User-Agent", "PVOutput/" + VERSION);
		
		return http;
	}
	
	public static String encodeValues(String s)
	{
		int offset = s.indexOf('?');
		StringBuffer q = new StringBuffer();
		q.append(s.substring(0, offset));
		if(offset  > 0)
		{
			q.append("?");
			String s1 = s.substring(offset+1);
			String[] pairs = s1.split("\\&");
			int n = 0;
			for(String pair: pairs)
			{
				String[] nameValue = pair.split("=");
				if(nameValue != null && nameValue.length == 2)
				{
					if(n > 0)
					{
						q.append("&");
					}
					q.append(nameValue[0]).append("=").append(Uri.encode(nameValue[1]));
					n++;
				}
			}
		}
		return q.toString();
	}
		
}