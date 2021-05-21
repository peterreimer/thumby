/*Copyright (c) 2015 "hbz"

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
package controllers;

import static helper.Globals.*;

import helper.ThumbnailGenerator;
import helper.TypedInputStream;
import helper.URLUtil;

import java.awt.Panel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.MediaType;
import com.google.inject.Inject;

import play.mvc.Http;
import play.mvc.Result;
import play.Environment;
import play.api.Configuration;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import views.html.*;

/**
 * @author Jan Schnasse
 * Refactoring: Alessio Pellerito
 *
 */
public class Application extends MyController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Environment environment;
    private final ThumbnailGenerator thumbnailGen;
    private final URLUtil urlUtil;
    private final Configuration config;
    
    private final FormFactory formFactory;
    
    @Inject
    public Application(Environment e, ThumbnailGenerator thumbnailGen, URLUtil urlUtil, Configuration config, FormFactory factory) {
        this.environment = e;
        this.thumbnailGen = thumbnailGen;
        this.urlUtil = urlUtil;
        this.config = config;
        this.formFactory = factory;
        
    }

    /**
     * @return a form to post a url parameter to the uploadUrl endpoint
     */

    // /*Result ist die Response des Servers an den Client
    //public CompletionStage<Result> registerUrlForm() { //Methodenname ist veränderbar, Programm funktioniert trotzdem
      //  return CompletableFuture.supplyAsync( (Supplier<Result>) () -> ok(uploadUrl.render(null)) );
    //}*/
    
    
    
    public Result inputReader(Http.Request req) {
        DynamicForm bindedForm = formFactory.form().bindFromRequest(req);
        
        String url = bindedForm.get("urlName");
        logger.warn(url);
        String size = bindedForm.get("sizeName");
        logger.warn(size);
        
        return ok("You sent: " + url + ", " + size);
    }
    

    /**
     * @param urlAddress
     *            a url encoded string of a valid url
     * @param size
     *            the size of the thumbnail
     * @return image/jpeg
     */
    public CompletionStage<Result> getThumbnail(String urlAddress, int size) {
        return CompletableFuture.supplyAsync( (Supplier<Result>) () -> {
            try {
                if (urlAddress == null || urlAddress.isEmpty()) {
                    logger.warn("UrlAdress1: " + urlAddress);
                    
                    return ok(uploadUrl.render());
                }
                
                URL url = new URL(urlAddress);
                if (!this.isWhitelisted(url.getHost())) {
                    logger.warn("UrlHost: " + url.getHost());
                    logger.warn("UrlAdress2: " + urlAddress);
                    return status(403, "thumby is not allowed to access this url!");
                }
                logger.warn("UrlAdress3: " + urlAddress);
                // In unserem Bildfall kommt sowas zurück /tmp/thumby-storage / 90 (Prüfsumme) / Encoded Url von https://mars.nasa.gov/system/resources/detail_files/25642_PIA24264-Panorama-supplemental-image-wind-carved-rock-web.jpg200
                File result = (File) storage.get(url.toString() + size);                      // aHR0cHM6Ly9tYXJzLm5hc2EuZ292L3N5c3RlbS9yZXNvdXJjZXMvZGV0YWlsX2ZpbGVzLzI1NjQyX1BJQTI0MjY0LVBhbm9yYW1hLXN1cHBsZW1lbnRhbC1pbWFnZS13aW5kLWNhcnZlZC1yb2NrLXdlYi5qcGcyMDA=
                if (result == null) {
                    // result ist eine temporäre Datei mit dem erstellten thumbnail, dateiformat sieht aus wie folgt: urlDateiName-thumby test
                    result = uploadUrl(url, size);
                    storage.set(url.toString() + size, result);
                }
                return ok(result).as("image/jpeg").withHeader("Content-Disposition", result.getName());
                
            } catch (Exception e) {
                return internalServerError(e.toString()).as("text/plain");
            }
        });
    }

    private boolean isWhitelisted(String host) {
        for (String w : whitelist) {
            if (w.equals(host))
                return true;
        }
        return false;
    }

    private File uploadUrl(URL url, int size) {
        TypedInputStream ts = null;
        try {
            File thumbnail = this.createThumbnail(environment.resourceAsStream(CONNECTION_ERROR_PIC), MediaType.PNG, size, url.toString());
            ts = urlUtil.urlToInputStream(url);
            thumbnail = this.createThumbnail(ts.in, MediaType.parse(ts.type), size, url.toString());
            return thumbnail;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ts != null && ts.in != null) {
                    ts.in.close();
                }
            } catch (Exception e) {
                logger.error("Problems to close connection! Maybe restart application to prevent too many open connections.\nCaused when accessing: " + url);
            }
        }
    }

    public File createThumbnail(InputStream ts, MediaType contentType, int size, String name) {
        logger.warn("Content-Type: " + contentType);
        File out = thumbnailGen.createThumbnail(ts, contentType, size, name);
        if (out == null) {
            out = thumbnailGen.createThumbnail(environment.resourceAsStream(THUMBNAIL_NULL_PIC),
                    MediaType.PNG, size, name);
        }

        return out;
    }

   
}
