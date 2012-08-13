package at.ainf.logger.framework;




import java.io.File;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 12.09.11
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
public class LogRotationAppender {
    private static final String ROTATION_COUNT_MARKER = "%u";
    private Integer rotationCount;
    private File directory;
    private String prefix;
    private String suffix;

    public LogRotationAppender() {
        super();
    }

    /* LogRotationAppender(Layout layout,
                            String directory,
                            String filename,
                            int rotationCount) {
        super();
        setLayout(layout);
        setDirectory(directory);
        setRelativeFile(filename);
        setRotationCount(rotationCount);
    }  */

    public void setDirectory(String dirName) {
        this.directory = new File(System.getProperty("user.home"), dirName);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                directory = null;
            }
        }
        initializeIfReady();
    }

    public void setRelativeFile(String filename) {
        int marker = filename.lastIndexOf(ROTATION_COUNT_MARKER);
        prefix = filename.substring(0, marker);
        suffix = filename.substring(marker + ROTATION_COUNT_MARKER.length());
        initializeIfReady();
    }

    public void setRotationCount(int rotationCount) {
        this.rotationCount = rotationCount;
        initializeIfReady();
    }

    public int getRotationCount() {
        return rotationCount;
    }

    private boolean configured() {
        return directory != null && prefix != null && rotationCount != null;
    }

    private void initializeIfReady() {
        if (configured()) {
            //setFile(getNextLogFile());
        }
    }

    private String getNextLogFile() {
        Map<Integer, File> matchingLogFiles = new HashMap<Integer, File>();
        int lastMatch = -1;
        for (File f : directory.getAbsoluteFile().listFiles()) {
            Integer count = matchingFile(f);
            if (count == null) continue;
            matchingLogFiles.put(count, f);
            if (lastMatch < count) {
                lastMatch = count;
            }
        }


        for (Map.Entry<Integer, File> entry : matchingLogFiles.entrySet()) {
            if (entry.getKey() <= lastMatch - rotationCount + 1) {
                entry.getValue().delete();
            }
        }
        String hostname = "UNKNOWN";
        try {
            hostname = Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return new File(directory, prefix + hostname + suffix).getAbsolutePath();

    }

    private Integer matchingFile(File f) {
        String relativeName = f.getName();
        if (!relativeName.startsWith(prefix) || !relativeName.endsWith(suffix) ||
                relativeName.length() < prefix.length() + suffix.length()) {
            return null;
        }
        String countString = relativeName.substring(prefix.length(), relativeName.length() - suffix.length());
        try {
            return Integer.parseInt(countString);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }


}
