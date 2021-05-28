/*Copyright (c) 2018 "hbz"

This file is part of thumby.

thumby is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package helper;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.CRC32;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jan Schnasse
 * Refactored by Alessio Pellerito
 */
public class Storage {

    private static String storageLocation = "/tmp/thumby-storage";
    private long partitions = 100;
    // show log messages
    private final ch.qos.logback.classic.Logger logger = 
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(this.getClass());

    public Storage(String locationPath) {
        if (locationPath != null) {
            storageLocation = locationPath;
        }
        logger.warn("Store content in: " + storageLocation);
        for(int i = 0; i <= partitions; i++){
            new File(storageLocation + File.separator + i).mkdirs(); 
        }
    }

    public File get(String key) {
        File target = findTarget(key);
        logger.warn("SEARCH "+ target);
        if (target.exists()) {
            return target;
        }
        return null;
    }
    
    public void set(String key, File result) {

        File target = findTarget(key);
        logger.warn("CREATE "+ target);
        try {
            Files.copy(result, target);
        } catch (IOException e) {
            logger.debug("",e);
            throw new RuntimeException("", e);
        }
    } 

    private File findTarget(String key) {
        String name = encode(key);
        String dirname = getDirName(name);
        return new File(storageLocation + File.separator + dirname + File.separator + name);
    }

    private String getDirName(String name) {
        /* CRC32 checksum calculation */
        CRC32 crc = new CRC32();
        crc.update(name.getBytes());
        long num = crc.getValue();
        long mod = num % partitions;
        String dirname = "" + mod;
        return dirname;
    }

    private String encode(String encodeMe) {
        return Base64.getUrlEncoder().encodeToString(encodeMe.getBytes());
    }

    private String decode(String decodeMe) {
        return new String(Base64.getUrlDecoder().decode(decodeMe));
    }
}
