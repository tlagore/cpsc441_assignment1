import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HttpHeader {
	private Integer _Status;
	private String _FileType;
	private Date _LastModified;
	
	public HttpHeader()
	{
		_Status = null;
		_FileType = null;
		_LastModified = null;
	}
	
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
					SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
					_LastModified = format.parse(lines[i].replace("Last-Modified: ", ""));
				}catch(ParseException ex)
				{
					//bad date format
				}
			}
		}
	}
	
	public HttpHeader(Integer status, String fileType, Date lastModified)
	{
		_Status = status;
		_FileType = fileType;
		_LastModified = lastModified;
	}
	
	public Integer get_Status() {
		return _Status;
	}

	public void set_Status(Integer _Status) {
		this._Status = _Status;
	}

	public String get_FileType() {
		return _FileType;
	}

	public void set_FileType(String _FileType) {
		this._FileType = _FileType;
	}

	public Date get_LastModified() {
		return _LastModified;
	}
	
	public long get_LastModifiedLong(){
		return _LastModified.getTime();
	}

	public void set_LastModified(Date _LastModified) {
		this._LastModified = _LastModified;
	}

}
