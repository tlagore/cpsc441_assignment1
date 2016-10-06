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
		
    /**
     * Default constructor to initialize data structures used for caching/etc
	 * If the cache already exists then load it. If any errors then throw exception.
	 *
     * @throws UrlCacheException if encounters any errors/exceptions
     */
	public UrlCache() throws UrlCacheException {
		String catalogDir = System.getProperty("user.dir") + "\\cache\\catalog";
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
		Scanner inputStream;
		PrintWriter outputStream;
		
		try
		{
			Socket socket = new Socket(url, DEFAULT_HTTP_PORT);
			outputStream = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
			inputStream = new Scanner(new InputStreamReader(socket.getInputStream()));
			
						
			
			inputStream.close();
			outputStream.close();
		}catch(Exception ex)
		{
			
		}
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
		
		try
		{
			//InetAddress addr = InetAddress.getByName("people.ucalgary.ca/~mghaderi/index.html");
			//System.out.println(addr.getHostAddress());
			String host = getHostnameFromUrl(url);
			String objectPath = getObjectPathFromUrl(url);
			String command = "GET " + objectPath + " HTTP/1.1\r\n";
			
			Socket socket = new Socket(host, DEFAULT_HTTP_PORT);  //explodes here, UnknownHostException
			outputStream = new PrintWriter(socket.getOutputStream());
			//ObjectInputStream objInStream = new ObjectInputStream(socket.getInputStream());
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			outputStream.print(command);
			outputStream.print("Host: " + host + "\r\n");
			outputStream.print("\r\n");
			outputStream.flush();
			
			while((response = inputStream.readLine()) != null)
				System.out.println(response);
			
			inputStream.close();
			outputStream.close();
			socket.close();
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
		String catalogDir = System.getProperty("user.dir") + "\\cache\\";
		Path path = Paths.get(catalogDir);
		
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
			FileOutputStream fileOut = new FileOutputStream(catalogDir + "\\catalog");
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
		return url.substring(0, url.indexOf("/") == -1 ? url.length() - 1 : url.indexOf("/"));
	}
	
	private String getObjectPathFromUrl(String url)
	{
		return url.indexOf("/") == -1 ? "" : url.substring(url.indexOf("/"), url.length());
	}
}
