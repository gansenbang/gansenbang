/**
 * 
 */
package cn.edu.scnu.s4;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.s4.persist.Persister;

/**
 * @author ChunweiXu
 *
 */
public class DirectToFilePersister implements Persister {
    private String outputFilename;
    private int persistCount;

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    @Override
    public int cleanOutGarbage() throws InterruptedException {
        return 0;
    }

    @Override
    public Object get(String arg0) throws InterruptedException {
        return null;
    }

    @Override
    public Map<String, Object> getBulk(String[] arg0)
            throws InterruptedException {
        return new HashMap<String, Object>();
    }

    @Override
    public Map<String, Object> getBulkObjects(String[] arg0)
            throws InterruptedException {
        return new HashMap<String, Object>();
    }

    @Override
    public int getCacheEntryCount() {
        return 1;
    }

    @Override
    public Object getObject(String arg0) throws InterruptedException {
        return null;
    }

    @Override
    public int getPersistCount() {
        return persistCount;
    }

    @Override
    public int getQueueSize() {
        return 0;
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<String>();
    }

    @Override
    public void remove(String arg0) throws InterruptedException {

    }

    @Override
    public void set(String key, Object value, int persistTime)
            throws InterruptedException {

        FileWriter fw = null;
        try {
            fw = new FileWriter(outputFilename);
            fw.write(String.valueOf(value));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Logger.getLogger("s4").error(e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void setAsynch(String key, Object value, int persistTime) {
        try {
            set(key, value, persistTime);
        } catch (InterruptedException ie) {
        }
    }

}

