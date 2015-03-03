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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import models.Thumbnail;
import play.cache.Cache;
import play.libs.F.Promise;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

import com.google.common.net.MediaType;

/**
 * @author Jan Schnasse
 *
 */
public class Application extends MyController {

    /**
     * @return the start page
     */
    public static Promise<Result> index() {
	return Promise.promise(() -> {
	    return ok(views.html.index.render(null));
	});
    }

    /**
     * @return a simple multipart form for file uploading
     */
    public static Promise<Result> uploadFileForm() {
	return Promise.promise(() -> {
	    return ok(views.html.uploadFile.render(null));
	});
    }

    /**
     * @param size
     *            the width of the target thumbnail. Defaults to 150px when
     *            called over HTTP.
     * @return supports "application/html","application/json", defaults to
     *         image/jpeg
     */
    public static Promise<Result> uploadFile(int size) {
	return Promise.promise(() -> {
	    MultipartFormData body = request().body().asMultipartFormData();
	    FilePart picture = body.getFile("file");
	    if (picture != null) {

		/*
		 * TODO use this name for delivery String fileName =
		 * picture.getFilename();
		 */
		MediaType type = MediaType.parse(picture.getContentType());
		File file = picture.getFile();
		Thumbnail thumbnail = createThumbnail(file, type, size);
		if (request().accepts("application/html"))
		    return ok(views.html.thumbnail.render(thumbnail));

		if (request().accepts("application/json"))
		    return getJsonResult(thumbnail);

		return getThumbnailAsResult(thumbnail.id);
	    } else {
		flash("error", "Missing file");
		return redirect(routes.Application.index());
	    }
	});
    }

    /**
     * @return a form to post a url parameter to the uploadUrl endpoint
     */
    public static Promise<Result> registerUrlForm() {
	return Promise.promise(() -> {
	    return ok(views.html.uploadUrl.render(null));
	});
    }

    /**
     * Must have a form encoded body with 'url=some-url-encoded-url' parameter
     * set.
     * 
     * @param size
     *            the width of the target thumbnail. Defaults to 150px when
     *            called over HTTP.
     * @return supports "application/html","application/json", defaults to
     *         image/jpeg
     */
    public static Promise<Result> uploadUrl(int size) {
	return Promise.promise(() -> {
	    try {
		Map<String, String[]> reqmap = request().body()
			.asFormUrlEncoded();
		String urlAddress = reqmap.get("url")[0];
		play.Logger.debug(urlAddress);
		URL url = new URL(urlAddress);
		HttpURLConnection connection = (HttpURLConnection) url
			.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		String contentType = connection.getContentType();
		Thumbnail thumbnail = createThumbnail(
			connection.getInputStream(),
			MediaType.parse(contentType), size);

		if (request().accepts("application/html"))
		    return ok(views.html.thumbnail.render(thumbnail));

		if (request().accepts("application/json"))
		    return getJsonResult(thumbnail);

		return getThumbnailAsResult(thumbnail.id);

	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	});
    }

    private static Thumbnail createThumbnail(File file, MediaType contentType,
	    int size) {
	try (InputStream in = new FileInputStream(file)) {
	    return createThumbnail(in, contentType, size);
	} catch (Exception e) {
	    throw new RuntimeException(e);
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

    /**
     * @param id
     *            a cache id
     * @return a promise to the raw thumb
     */
    public static Promise<Result> getThumbnail(String id) {
	return Promise.promise(() -> {
	    return getThumbnailAsResult(id);
	});
    }

    /**
     * @param id
     *            cache id
     * @return the raw thumbnail
     */
    public static Result getThumbnailAsResult(String id) {

	Thumbnail result = (Thumbnail) Cache.get(id);
	if (result == null)
	    return status(404, "404 Not Found");
	return ok(result.thumb);

    }

}
