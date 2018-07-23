package helper;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import com.google.common.io.Files;

import play.Play;

public class Storage {

    private static String storageLocation = "/tmp/thumby-storage";
    
    public Storage(String loc){       
        if(loc!=null){
            storageLocation=loc; 
        }
        play.Logger.debug("Store content in: "+storageLocation);
        new File(storageLocation).mkdirs();
    }

    public File get(String key) {
        String name = encode(key);
        File target = new File(storageLocation + File.separator + name);
        if (target.exists()) {
            return target;
        }
        return null;
    }

    public void set(String key, File result) {
        String name = encode(key);
        File target = new File(storageLocation + File.separator + name);
        try {
            Files.copy(result, target);
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }
    }

    private static String encode(String encodeMe) {
        return Base64.getEncoder().encodeToString(encodeMe.getBytes()).replaceAll("/", "-").replaceAll("\\+", "_");
    }

    private static String decode(String decodeMe) {
        String base64EncodedName = decodeMe.replaceAll("-", "/").replaceAll("_", "+");
        return new String(Base64.getDecoder().decode(base64EncodedName));
    }
}
