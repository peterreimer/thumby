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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import models.Thumbnail;
import play.Play;
import play.cache.Cache;
import play.libs.F.Promise;
import play.mvc.Result;

import com.google.common.net.MediaType;

/**
 * @author Jan Schnasse
 *
 */
public class Application extends MyController {

    static final int CACHE_EXPIRATION = 60 * 60;

    /**
     * @return a form to post a url parameter to the uploadUrl endpoint
     */
    public static Promise<Result> registerUrlForm() {
        return Promise.promise(() -> {
            return ok(views.html.uploadUrl.render(null));
        });
    }

    /**
     * @param urlAddress
     *            a url encoded string of a valid url
     * @param size
     *            the size of the thumbnail
     * @return image/jpeg
     */
    public static Promise<Result> getThumbnail(String urlAddress, int size) {
        return Promise.promise(() -> {
            try {
                if (urlAddress == null || urlAddress.isEmpty())
                    return ok(views.html.uploadUrl.render(null));
                URL url = new URL(urlAddress);
                if (!isWhitelisted(url.getHost()))
                    return status(403, "thumby is not allowed to access this url!");
                Thumbnail result = (Thumbnail) Cache.get(url.toString() + size);
                if (result == null) {
                    result = uploadUrl(url, size);
                    Cache.set(url.toString() + size, result, CACHE_EXPIRATION);
                }
                response().setHeader("Content-Disposition", result.name);
                response().setHeader("Content-Type", "image/jpeg");
                return ok(result.thumb);
            } catch (Exception e) {
                response().setHeader("Content-Type", "text/plain");
                return internalServerError(e.toString());
            }
        });
    }

    private static boolean isWhitelisted(String host) {
        for (String w : whitelist) {
            if (w.equals(host))
                return true;
        }
        return false;
    }

    private static Thumbnail uploadUrl(URL url, int size) throws Exception {
        Thumbnail thumbnail = createThumbnail(Play.application().resourceAsStream(CONNECTION_ERROR_PIC), MediaType.PNG,
                size, url.toString());
        TypedInputStream ts = urlToInputStream(url);
        thumbnail = createThumbnail(ts.in, MediaType.parse(ts.type), size, url.toString());
        return thumbnail;
    }

    static TypedInputStream urlToInputStream(URL url) {
        HttpURLConnection con = null;
        TypedInputStream ts = new TypedInputStream();
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(false);
            con.connect();
            ts.type = con.getContentType();
            int responseCode = con.getResponseCode();
            play.Logger.debug("Get a " + responseCode + " from " + url.toExternalForm());
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == 307 || responseCode == 303) {
                String redirectUrl = con.getHeaderField("Location");
                try {
                    URL newUrl = new URL(redirectUrl);
                    play.Logger.debug("Redirect to Location: " + newUrl);
                    return urlToInputStream(newUrl);
                } catch (MalformedURLException e) {
                    URL newUrl = new URL(url.getProtocol() + "://" + url.getHost() + redirectUrl);
                    play.Logger.debug("Redirect to Location: " + newUrl);
                    return urlToInputStream(newUrl);
                }
            }
            ts.in = con.getInputStream();
            return ts;
        } catch (IOException e) {
            play.Logger.debug("", e);
            throw new RuntimeException(e);
        }
    }

    public static Thumbnail createThumbnail(InputStream in, MediaType contentType, int size, String name) {

        play.Logger.debug("Content-Type: " + contentType);
        Thumbnail result = new Thumbnail();
        result.id = UUID.randomUUID().toString();
        result.thumb = ThumbnailGenerator.createThumbnail(in, contentType, size, name);
        if (result.thumb == null) {
            result.thumb = ThumbnailGenerator.createThumbnail(Play.application().resourceAsStream(THUMBNAIL_NULL_PIC),
                    MediaType.PNG, size, name);
        }
        result.name = name;
        result.originalContentType = contentType.toString();
        return result;
    }
}
