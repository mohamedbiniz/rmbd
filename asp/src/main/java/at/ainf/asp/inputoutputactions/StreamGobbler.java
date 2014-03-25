package at.ainf.asp.inputoutputactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import at.ainf.asp.test.Application;

public class StreamGobbler extends Thread {

    private static Logger logger = LoggerFactory.getLogger(StreamGobbler.class.getName());

    InputStream is;
    String type;
    String output;

    public StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
        this.output = "";
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();
            String line=null;
            while ( (line = br.readLine()) != null) {
                if (Application.enableInfo) { System.out.println(line); }
                buf.append(line).append("\n");
            }
            output = buf.toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String getOutput() {
        return output;
    }
}
