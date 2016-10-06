import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;


/**
 * UrlCache Class
 * 
 * @author 	Majid Ghaderi, Tyrone Lagore
 * @version	1.2, October 5, 2016
 *
 */
public class UrlCache {
	private HashMap<String, Long> _Catalog;
	private final int DEFAULT_HTTP_PORT = 80;
	private final String CACHE_DIR = System.getProperty("user.dir") + "\\cache\\";
		
    /**
     * Default constructor to initialize data structures used for caching/etc
	 * If the cache already exists then load it. If any errors then throw exception.
	 *
     * @throws UrlCacheException if encounters any errors/exceptions
     */
	public UrlCache() throws UrlCacheException {
		String catalogDir = CACHE_DIR + "catalog";
		Path path = Paths.get(catalogDir);
		
		System.out.println("Checking if catalog exists...");
		if(Files.exists(path))
		{
			try{
				FileInputStream fileIn = new FileInputStream(catalogDir);
				ObjectInputStream objIn = new ObjectInputStream(fileIn);
				
				_Catalog = (HashMap<String, Long>)objIn.readObject();
				
				objIn.close();
				fileIn.close();
			}catch(FileNotFoundException ex)
			{
				//file not found, shouldn't happen, we just checked if it existed.
				System.out.println("Error reading catalog.  Error:" + ex.getMessage() + ". Creating empty catalog.");
				_Catalog = new HashMap<String, Long>();
			}catch(IOException ex)
			{
				//IOException opening input stream
				System.out.println("Error reading catalog. Error:" + ex.getMessage() + ". Creating empty catalog.");
				_Catalog = new HashMap<String, Long>();
			}catch(ClassNotFoundException ex)
			{
				//ClassNotFoundException, shouldn't happen
				System.out.println("Error reading catalog. Error:" + ex.getMessage() + ". Creating empty catalog.");
				_Catalog = new HashMap<String, Long>();
			}
			
			
			//_Catalog = (HashMap<String, Long>)
			//read in catalog
			
		}else
		{
			_Catalog = new HashMap<String, Long>();
			//create new catalog
		}
		
	}
	
    /**
     * Downloads the object specified by the parameter url if the local copy is out of date.
	 *
     * @param url	URL of the object to be downloaded. It is a fully qualified URL.
     * @throws UrlCacheException if encounters any errors/exceptions
     */
	public void getObject(String url) throws UrlCacheException {
		BufferedReader inputStream;
		PrintWriter outputStream;
		String response, 
			headerInfo = "", 
			data = "",
			command;
		HttpHeader header;
		
		String standardizedUrl = getHostnameFromUrl(url) + getObjectPathFromUrl(url);
		String host = getHostnameFromUrl(url);
		String objectPath = getObjectPathFromUrl(url);
		
		//if we have the item, conditional get, else regular get
		if(_Catalog.containsKey(standardizedUrl))
		{
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			Date dDate = new Date(_Catalog.get(standardizedUrl));
			command = "GET " + objectPath + " HTTP/1.1 If-Modified-Since: " + format.format(dDate) + "\r\n";
		}else
		{
			command = "GET " + objectPath + " HTTP/1.1\r\n";
		}
		
		try
		{				
			Socket socket = new Socket(host, DEFAULT_HTTP_PORT);
			outputStream = new PrintWriter(socket.getOutputStream());
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			outputStream.print(command);
			outputStream.print("Host: " + host + "\r\n");
			outputStream.print("\r\n");
			outputStream.flush();
			
			//reads until a blank line is found signifying the end of the header
			response = inputStream.readLine();
			while(response != null && response.compareTo("") != 0)
			{
				System.out.println(response);
				headerInfo += response + "\r\n";
				response = inputStream.readLine();
			}
			
			header = new HttpHeader(headerInfo);
			
			//not at end of get request and GET request was good
			if(response != null && header.get_Status() == 200)
			{
				response = inputStream.readLine();
				while(response != null)
				{
					data += response;
					response = inputStream.readLine();
				}
				
				createDirectoryAndFile(objectPath, data);
				_Catalog.put(standardizedUrl, header.get_LastModifiedLong());
			}
			
			inputStream.close();
			outputStream.close();
			socket.close();
		}catch(Exception ex)
		{
			
		}
			
		
	}
	
	private void createDirectoryAndFile(String objectPath, String data)
	{
		
	}
	
    /**
     * Returns the Last-Modified time associated with the object specified by the parameter url.
	 *
     * @param url 	URL of the object 
	 * @return the Last-Modified time in millisecond as in Date.getTime()
     * @throws UrlCacheException if the specified url is not in the cache, or there are other errors/exceptions
     */
	public long getLastModified(String url) throws UrlCacheException {
		BufferedReader inputStream;
		PrintWriter outputStream;
		String response;
		String headerInfo = "";
		HttpHeader header;
		
		try
		{
			String host = getHostnameFromUrl(url);
			String objectPath = getObjectPathFromUrl(url);
			String command = "GET " + objectPath + " HTTP/1.1\r\n";
			
			Socket socket = new Socket(host, DEFAULT_HTTP_PORT);
			outputStream = new PrintWriter(socket.getOutputStream());
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			outputStream.print(command);
			outputStream.print("Host: " + host + "\r\n");
			outputStream.print("\r\n");
			outputStream.flush();
			
			//reads until a blank line is found signifying the end of the header
			response = inputStream.readLine();
			while(response != null && response.compareTo("") != 0)
			{
				System.out.println(response);
				headerInfo += response + "\r\n";
				response = inputStream.readLine();
			}
			
			header = new HttpHeader(headerInfo);
			
			inputStream.close();
			outputStream.close();
			socket.close();
			
			//if lastModified is null, header text did not contain last modified date
			if(header.get_LastModified() == null)
				return 0;
			else
				return header.get_LastModifiedLong();
		}catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
		return 0;
	}
	
	/**
	 * Close the class, writing the hashmap to file
	 */
	public void Close()
	{
		Path path = Paths.get(CACHE_DIR);
		
		if(!Files.exists(path))
		{
			try{
				Files.createDirectories(path);
			}catch(IOException ex)
			{
				System.out.println("Error creating catalog directory.  Error:" + ex.getMessage() + ". Reopening this application will create new catalog.");
			}
		}
		
		try{
			FileOutputStream fileOut = new FileOutputStream(CACHE_DIR + "\\catalog");
			ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			
			objOut.writeObject(_Catalog);
			
			objOut.close();
			fileOut.close();
		}catch(IOException ex)
		{
			System.out.println("Error writing catalog to file.  Error:" + ex.getMessage() + ". Reopening this application will create new catalog.");
		}
	}
	
	private String getHostnameFromUrl(String url)
	{
		url = url.toLowerCase();
		url = url.replace("https://", "").replace("http://", "");
		url = url.substring(0, url.indexOf("/") == -1 ? url.length() - 1 : url.indexOf("/"));
		return url;
	}
	
	private String getObjectPathFromUrl(String url)
	{
		url = url.toLowerCase();
		url = url.replace("https://", "").replace("http://", "");
		return url.indexOf("/") == -1 ? "" : url.substring(url.indexOf("/"), url.length());
	}
}
