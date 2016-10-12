import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;


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
	private final TimeZone _SystemTimeZone = TimeZone.getDefault();
		
    /**
     * Default constructor to initialize data structures used for caching/etc
	 * If the cache already exists then load it. If any errors then throw exception.
	 *
     * @throws UrlCacheException if encounters any errors/exceptions
     */
	@SuppressWarnings("unchecked")
	public UrlCache() throws UrlCacheException {
		String catalogDir = CACHE_DIR + "catalog";
		Path path = Paths.get(catalogDir);
		
		System.out.println("Checking if catalog exists...");
		if(Files.exists(path))
		{
			//file exists, read in catalog
			System.out.println("Catalog exists, reading in cache catalog.");
			try{
				FileInputStream fileIn = new FileInputStream(catalogDir);
				ObjectInputStream objIn = new ObjectInputStream(fileIn);
				
				_Catalog = (HashMap<String, Long>)objIn.readObject();
				
				objIn.close();
				fileIn.close();
				System.out.println("Catalog successfully read. Ready to proceed.");
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
		byte[] input = new byte[10*1024];
		Socket socket;
		InputStream inputStream;
		PrintWriter outputStream;
		String headerInfo = "", 
			command,
			host = getHostnameFromUrl(url),
			objectPath = getObjectPathFromUrl(url);
		HttpHeader header;
		int amountRead;
		host = host.indexOf(":") == -1 ? host : host.substring(0, host.indexOf(":"));
		String standardizedUrl = host + objectPath;
			
		command = getHttpGetCommand(url);
		
		try
		{				
			socket = getSocket(url);
			outputStream = new PrintWriter(socket.getOutputStream());
			inputStream = socket.getInputStream();

			outputStream.print(command);
			outputStream.print("Host: " + host + "\r\n");
			outputStream.print("\r\n");
			outputStream.flush();
			
			amountRead = inputStream.read(input);
			
			if(amountRead != -1 && amountRead != 0)
			{
				headerInfo = extractHeaderInfo(input);
				
				String str1 = new String(input);
				System.out.println(str1);
				
				header = new HttpHeader(headerInfo, _SystemTimeZone);
				if (header.get_Status() == 200)
				{
					File file = createDirectoryAndFile(standardizedUrl);
					FileOutputStream fileOut = new FileOutputStream(file, true);
					fileOut.write(input);
					
					//read rest of data
					while((amountRead = inputStream.read(input)) != -1)
					{
						fileOut.write(input);
					}
					
					fileOut.close();
					_Catalog.put(standardizedUrl, header.get_LastModifiedLong());
				}else if (header.get_Status() == 304)
				{
					System.out.println("Http: " + header.get_Status() + ". Same or newer file stored in cache.");
				}else
				{
					//some other code
					System.out.println("Error. Http status code: " + header.get_Status() + ".");
				}
			}else
			{
				System.out.println("No data read from response.");
			}
		
			outputStream.close();
			socket.close();
		}catch(Exception ex)
		{
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	private Socket getSocket(String url) throws IOException
	{
		Socket socket = null;
		String host = getHostnameFromUrl(url);
		int indexOfColon = host.indexOf(":");
		int port;
		
		try{
			port = Integer.parseInt(indexOfColon == -1 ? "" : host.substring(indexOfColon));
		}catch(Exception ex)
		{
			port = -1;
		}
		host = indexOfColon == -1 ? host : host.substring(0, indexOfColon);
			
		try{
			socket = new Socket(host, port == -1 ? DEFAULT_HTTP_PORT : port);
		}catch(IOException ex)
		{
			throw ex;
		}
		
		return socket;
	}

	/**
	 * 
	 * 
	 * @param url 
	 * @param objectPath
	 * @return
	 */
	private String getHttpGetCommand(String url) {
		String command;
		Long lastModified;
		
		//if we have the item cached, conditional get, else regular 
		try{
			//add 1 second
			lastModified = getLastModified(url);
			SimpleDateFormat dateFormat = new SimpleDateFormat(
			        "EEE, dd MMM yyyy HH:mm:ss zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date dDate = new Date(lastModified);
			command = "GET " + getObjectPathFromUrl(url) + " HTTP/1.1\r\nIf-Modified-Since: " + dateFormat.format(dDate) + "\r\n";
		}catch(UrlCacheException ex)
		{
			System.out.println(ex.getMessage());
			command = "GET " + getObjectPathFromUrl(url) + " HTTP/1.1\r\n";
		}
		return command;
	}
	
	
	/**
	 * 
	 * @param data
	 * @return returns the header information in string format
	 */
	private String extractHeaderInfo(byte[] data)
	{
		String header = "";
		String line;
		InputStream inputStream = new ByteArrayInputStream(data);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		int dataLength = data.length;
		
		try
		{
			line = reader.readLine();
			while (line != null && !line.isEmpty())
			{
				header += line + "\r\n";
				line  = reader.readLine();
			}
			
			// add 2 to account for \r\n line that was not read in
			int bytesInHeader = header.getBytes("UTF-8").length + 2;
			
			for (int i = 0; i < (dataLength - bytesInHeader); i++)
			{
				data[i] = data[i + bytesInHeader];
			}
			
			for (int i = (dataLength - bytesInHeader); i < dataLength; i++)
			{
				data[i] = 0;
			}
		}catch(IOException ex)
		{
			
		}
		
		
		return header;
	}
	
	/**
	 * Takes the standardized url (url minus https:// or http://) and creates the file and directory for it
	 * 
	 * @param urlPath the full object path of the item on the server
	 */
	private File createDirectoryAndFile(String urlPath)
	{
		String dirPath = CACHE_DIR + urlPath;
		File file = null;
		try{
			file = new File(dirPath);
			file.delete();
			file.getParentFile().mkdirs();
			file.createNewFile();
		}catch(Exception ex)
		{
			System.out.println("Error creating file directory.  Error:" + ex.getMessage() + ". File not saved.");
		}
		
		return file;
	}
	
    /**
     * Returns the Last-Modified time associated with the object specified by the parameter url.
	 *
     * @param url 	URL of the object 
	 * @return the Last-Modified time in millisecond as in Date.getTime()
     * @throws UrlCacheException if the specified url is not in the cache, or there are other errors/exceptions
     */
	public long getLastModified(String url) throws UrlCacheException {
		String standardizedUrl = getHostnameFromUrl(url) + getObjectPathFromUrl(url);
		if(_Catalog.containsKey(standardizedUrl))
			return _Catalog.get(standardizedUrl);
		else
			throw new UrlCacheException("URL not in cache.");
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
