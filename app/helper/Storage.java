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
 *
 */
public class Storage {

    private static String storageLocation = "/tmp/thumby-storage";
    private long partitions = 100;
    // show log messages
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
    
    // Bei set wird das File Object target lokal (innerhalb der Methode) erstellt, warum ist es dann global sichtbar? !kein Rückgabewert
    public void set(String key, File result) {
        // target ist die neue erstellte Datei im richtigen Verzeichnis und dem richtigen Dateiname: /tmp/thumby-storage / Prüfsumme (CRC) / Encoded URL
        File target = findTarget(key);
        logger.warn("CREATE "+ target);
        try {
            // Wird nur der Inhalt der Dateien kopiert? Nicht der Name?
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
        /* CRC32 ist für Berechnung der Prüfsumme zuständig, um Bitfehler zu vermeiden
         * In diesem Fall ist die Prüfsumme = den Ordner, indem die Datei hineinkommt
        */
        CRC32 crc = new CRC32();
        crc.update(name.getBytes());
        long num = crc.getValue();
        long mod = num % partitions;
        String dirname = "" + mod;
        return dirname;
    }

    private String encode(String encodeMe) {
        return Base64.getUrlEncoder().encodeToString(encodeMe.getBytes())/*.replaceAll("/", "-").replaceAll("\\+", "_")*/;
    }

    private String decode(String decodeMe) {
        //String base64EncodedName = decodeMe.replaceAll("-", "/").replaceAll("_", "+");
        return new String(Base64.getUrlDecoder().decode(decodeMe));
    }
}
