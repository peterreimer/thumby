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

/**
 * 
 * @author Jan Schnasse
 *
 */
public class Storage {

    private static String storageLocation = "/tmp/thumby-storage";
    private long partitions =100;;

    public Storage(String loc) {
        if (loc != null) {
            storageLocation = loc;
        }
        play.Logger.debug("Store content in: " + storageLocation);
        for(int i=0;i<=partitions;i++){
            new File(storageLocation+File.separator+i).mkdirs(); 
        }
    }

    public File get(String key) {
        File target = findTarget(key);
        play.Logger.debug("SEARCH "+target);
        if (target.exists()) {
            return target;
        }
        return null;
    }

    public void set(String key, File result) {
        File target = findTarget(key);
        play.Logger.debug("CREATE "+target);
        try {
            Files.copy(result, target);
        } catch (IOException e) {
            play.Logger.debug("",e);
            throw new RuntimeException("", e);
        }
    } 

    private File findTarget(String key) {
        String name = encode(key);
        String dirname = getDirName(name);
        return new File(storageLocation + File.separator + dirname + File.separator + name);
    }

    private String getDirName(String name) {
        CRC32 crc = new CRC32();
        crc.update(name.getBytes());
        long num = crc.getValue();
        long mod = num % partitions;
        String dirname = "" + mod;
        return dirname;
    }

    private static String encode(String encodeMe) {
        return Base64.getEncoder().encodeToString(encodeMe.getBytes()).replaceAll("/", "-").replaceAll("\\+", "_");
    }

    private static String decode(String decodeMe) {
        String base64EncodedName = decodeMe.replaceAll("-", "/").replaceAll("_", "+");
        return new String(Base64.getDecoder().decode(base64EncodedName));
    }
}
