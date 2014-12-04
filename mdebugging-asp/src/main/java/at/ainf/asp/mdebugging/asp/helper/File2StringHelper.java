/**
 * 
 */
package at.ainf.asp.mdebugging.asp.helper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Melanie Frühstück
 *
 */
public class File2StringHelper {
	
	/**
	 * Converts a logic program given in a file into a string representation.
	 * @param filePath
	 * @return the string
	 */
	public static String convertFromFileToString(String filePath) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
		    
		    String ls = System.getProperty("line.separator");
		    
		    while((line = reader.readLine()) != null ) {
			    stringBuilder.append(line);
			    stringBuilder.append(ls);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File was not found: " + e);
		} catch (IOException e) {
			e.printStackTrace();
		}

	    return stringBuilder.toString();
	}

	
}
