
/**
 * A simple test driver
 * 
 * @author 	Majid Ghaderi
 * @version	3.1, Sep 30, 2016
 *
 */
public class Tester {
	
	public static void main(String[] args) {

		try{
			UrlCache cache = new UrlCache();
			
			//cache.getLastModified("people.ucalgary.ca/~mghaderi/index.html");
			cache.getObject("people.ucalgary.ca/~mghaderi/index.html");
			//cache.getLastModified("people.ucalgary.ca/~mghaderi/test/uc.gif");
			//cache.getLastModified("https://www.cbc.ca/radio/podcasts/index.html");
			cache.Close();
		}catch(UrlCacheException ex)
		{
			
		}
		
		
//		// include whatever URL you like
//		// these are just some samples
//		String[] url = {"people.ucalgary.ca/~mghaderi/index.html",
//						"people.ucalgary.ca/~mghaderi/test/uc.gif",
//						"people.ucalgary.ca/~mghaderi/test/a.pdf",
//						"people.ucalgary.ca:80/~mghaderi/test/test.html"};
//		
//		// this is a very basic tester
//		// the TAs will use a more comprehensive set of tests
//		try {
//			UrlCache cache = new UrlCache();
//			
//			for (int i = 0; i < url.length; i++)
//				cache.getObject(url[i]);
//			
//			System.out.println("Last-Modified for " + url[0] + " is: " + cache.getLastModified(url[0]));
//			cache.getObject(url[0]);
//			System.out.println("Last-Modified for " + url[0] + " is: " + cache.getLastModified(url[0]));
//			
//			cache.Close();
//		}
//		catch (UrlCacheException e) {
//			System.out.println("There was a problem: " + e.getMessage());
//		}
		
	}
	
}
