/**
 * 
 */
package at.ainf.asp.ioactions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

import at.ainf.asp.model.IProgramElement;

/**
 * @author Melanie Frühstück
 *
 */
public class ASPConverter {

	protected char[] data;
	
	public ASPConverter() {
		
	}
	
	/**
	 * Converts the program into a string.
	 * @param program
	 * @return the program as string
	 */
	public String convertFromProgramToString(Set<IProgramElement> program) {
		StringBuffer buf = new StringBuffer();
		for (IProgramElement pe : program) {
			buf.append(pe.getString()).append("\n");
		}
		return buf.toString();
	}
	
	/**
	 * Converts the data of logic program file into a string.
	 * @param filePath
	 * @return the program given in a file into a string
	 */
	public String convertFromFileToString(String filePath) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
		} catch (FileNotFoundException e) {
			System.out.println("File was not found: " + e);
		}
	    String line = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    String ls = System.getProperty("line.separator");

	    try {
			while((line = reader.readLine()) != null ) {
			    stringBuilder.append(line);
			    stringBuilder.append(ls);
			}
		} catch (IOException e) {
			System.out.println("Problems with reading lines of file: " + e);
		}

	    return stringBuilder.toString();
	}

	/**
	 * Writes the program into a temporary file for solving the intermediate program.
	 * @param program
	 * @return the path of the file
	 * @throws IOException
	 */
	public String write(Set<IProgramElement> program) throws IOException {
		String prog = convertFromProgramToString(program);
		data = prog.toCharArray();
		
		String tmpString = System.getProperty("java.io.tmpdir");
		File tmp = new File(tmpString);
		File f = File.createTempFile("post", ".lp", tmp);
		f.deleteOnExit();
		
		write(f);
//		System.out.println(f.getAbsolutePath());
		return f.getAbsolutePath();
	}

	/**
	 * Writes the data into a file.
	 * @param f
	 * @throws IOException
	 */
	private void write(File f) throws IOException {
		OutputStreamWriter osw;
		FileOutputStream fos = new FileOutputStream(f);
		osw = new OutputStreamWriter(fos);
		try {
			osw.write(data);
		} finally {
			osw.close();
		}
	}
	
}
