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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.net.MediaType;

import play.Play;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * @author Jan Schnasse
 *
 */
public class Application extends MyController {

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
                File result = (File) storage.get(url.toString() + size);
                if (result == null) {
                    result = uploadUrl(url, size);
                    storage.set(url.toString() + size, result);
                }
                response().setHeader("Content-Disposition", result.getName());
                response().setHeader("Content-Type", "image/jpeg");
                return ok(result);
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

    private static File uploadUrl(URL url, int size) {
        TypedInputStream ts = null;
        try {
            File thumbnail = createThumbnail(Play.application().resourceAsStream(CONNECTION_ERROR_PIC), MediaType.PNG,
                    size, url.toString());
            ts = urlToInputStream(url);
            thumbnail = createThumbnail(ts.in, MediaType.parse(ts.type), size, url.toString());
            return thumbnail;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ts != null && ts.in != null) {
                    ts.in.close();
                }
            } catch (Exception e) {
                play.Logger
                        .error("Problems to close connection! Maybe restart application to prevent too many open connections.\nCaused when accessing: "
                                + url);
            }
        }
    }

    public static File createThumbnail(InputStream ts, MediaType contentType, int size, String name) {
        play.Logger.debug("Content-Type: " + contentType);
        File out = ThumbnailGenerator.createThumbnail(ts, contentType, size, name);
        if (out == null) {
            out = ThumbnailGenerator.createThumbnail(Play.application().resourceAsStream(THUMBNAIL_NULL_PIC),
                    MediaType.PNG, size, name);
        }

        return out;
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
}
