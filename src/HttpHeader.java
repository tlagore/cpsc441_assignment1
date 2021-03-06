import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Class HttpHeader
 * 
 * Handles the http header information returned from an Http request
 * @author Tyrone
 *
 */
public class HttpHeader {
	private Integer _Status;
	private String _FileType;
	private Calendar _LastModified;

	
	/**
	 * Extracts the contents of a header given in string format (assumed to be separated by new lines)
	 * 
	 * @param headerText a string containing header information of a GET request.  Assumed to be in good format.
	 */
	public HttpHeader(String headerText)
	{
		//set all to null, if any information is missing from header, value will be null
		_Status = null;
		_FileType = null;
		_LastModified = null;
		
		String[] lines = headerText.split(System.getProperty("line.separator"));
		for(int i = 0; i < lines.length; i++)
		{
			//HTTP status message is in the form HTTP/1.1 XXX Readable Message
			//By removing "HTTP/1.1 " then taking the next 3 characters and converting it to an int, we can obtain the status code
			if (lines[i].contains("HTTP/1.1"))
				_Status = Integer.parseInt(lines[i].replace("HTTP/1.1 ","").substring(0,3));	
			
			if(lines[i].contains("Content-Type"))
				_FileType = lines[i].replace("Content-Type: ", "");
			
			if(lines[i].contains("Last-Modified"))
			{
				try{
					SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
					format.setTimeZone(TimeZone.getTimeZone("GMT"));
					String dateModified = lines[i].replaceAll("Last-Modified: ", "");
					
					_LastModified = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
					_LastModified.setTime(format.parse(dateModified));
				}catch(ParseException ex)
				{
					//bad date format
				}
			}
		}
	}
	
	/**
	 * Alternate constructor if no header text is available yet.
	 */
	public HttpHeader()
	{
		_Status = null;
		_FileType = null;
		_LastModified = null;
	}
	
	/**
	 * Alternate constructor if information is known
	 * @param status status of the http request
	 * @param fileType filetype of the http request
	 * @param lastModified when the file was last modified (if relevant)
	 */
	public HttpHeader(Integer status, String fileType, Calendar lastModified)
	{
		_Status = status;
		_FileType = fileType;
		_LastModified = lastModified;
	}
	
	/**	
	 * For the purposes of this simple client
	 * 200 OK
	 * 304 Not Modified
	 * 400 Bad Request
	 * 404 Not Found
	 * 
	 * @return the status code of the http get request
	 */
	public Integer get_Status() {
		return _Status;
	}
	
	/**	
	 * @return the file type of the get request, example: text/html
	 */
	public String get_FileType() {
		return _FileType;
	}


	/**	
	 * @return gets the Calendar format of the date
	 */
	public Calendar get_LastModified() {
		return _LastModified;
	}
	
	/**
	 * @return returns 0 if _LastModified has not been set otherwise time in milliseconds
	 */
	public long get_LastModifiedLong(){
		return _LastModified == null ? 0 : _LastModified.getTimeInMillis();
	}
}
