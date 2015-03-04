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

import helper.ThumbnailGenerator;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import models.Thumbnail;
import play.cache.Cache;
import play.libs.F.Promise;
import play.mvc.Result;

import com.google.common.net.MediaType;

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
     * @param url
     *            a url encoded string of a valid url
     * @param size
     *            the size of the thumbnail
     * @return image/jpeg
     */
    public static Promise<Result> getThumbnail(String url, int size) {
	return Promise.promise(() -> {
	    if (url == null)
		return ok(views.html.uploadUrl.render(null));
	    Thumbnail result = (Thumbnail) Cache.get(url + size);
	    if (result == null) {
		return uploadUrl(url, size);
	    }
	    return ok(result.thumb);
	});
    }

    private static Result uploadUrl(String urlAddress, int size) {
	try {
	    URL url = new URL(urlAddress);
	    HttpURLConnection connection = (HttpURLConnection) url
		    .openConnection();
	    connection.setRequestMethod("GET");
	    connection.connect();
	    String contentType = connection.getContentType();
	    Thumbnail thumbnail = createThumbnail(connection.getInputStream(),
		    MediaType.parse(contentType), size);
	    Cache.set(urlAddress + size, thumbnail);
	    response().setHeader("Content-Disposition", url.getPath());
	    response().setHeader("Content-Type", "image/jpeg");
	    return ok(thumbnail.thumb);
	} catch (Exception e) {
	    return internalServerError(e.toString());
	}
    }

    private static Thumbnail createThumbnail(InputStream in,
	    MediaType contentType, int size) {
	Thumbnail result = new Thumbnail();
	result.id = UUID.randomUUID().toString();
	result.thumb = ThumbnailGenerator
		.createThumbnail(in, contentType, size);
	result.url = "/" + result.id;
	result.originalContentType = contentType.toString();
	Cache.set(result.id, result);
	return result;
    }
}
