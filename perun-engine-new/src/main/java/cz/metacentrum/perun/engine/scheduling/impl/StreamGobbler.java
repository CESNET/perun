package cz.metacentrum.perun.engine.scheduling.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class StreamGobbler extends Thread {

    private InputStream is;
    private StringBuilder sb;
    private final static Logger log = LoggerFactory.getLogger(StreamGobbler.class);
    
    StreamGobbler(InputStream is) {
        super("StreamGobbler");
        this.sb = new StringBuilder();
        this.is = is;
    }

    public String getSb() {
        return sb.toString();
    }   
    
    public void run() {
        BufferedReader br = null;
        try {
            InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null) {
                this.sb.append(line);
                this.sb.append("\n");
            }   
         } catch (IOException e) {
             e.printStackTrace();  
         } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.error(e.toString(), e);
            }
         }
    }
}
