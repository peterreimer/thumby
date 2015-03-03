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
package helper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;

import com.google.common.io.Files;
import com.google.common.net.MediaType;

/**
 * Credits: https://github.com/benjaminpick/java-thumbnailer regain/Thumbnailer
 * - A file search engine providing plenty of formats (Plugin) Copyright (C)
 * 2011 Come_IN Computerclubs (University of Siegen)
 * 
 * 
 * 
 * @author Jan Schnasse
 *
 */
public class ThumbnailGenerator {
    private static final Color TRANSPARENT_WHITE = new Color(255, 255, 255, 0);

    /**
     * @param in
     *            the actual content to create a thumbnail from
     * @param contentType
     *            the MediaType for the content
     * @param size
     *            acually the width
     * @return a thumbnail file
     */
    public static File createThumbnail(InputStream in, MediaType contentType,
	    int size) {
	if (contentType.is(MediaType.ANY_IMAGE_TYPE)) {
	    return generateThumbnailFromImage(in, size);
	}
	if (contentType.is(MediaType.PDF)) {
	    return generateThumbnailFromPdf(in, size);
	}
	return null;
    }

    private static File generateThumbnailFromPdf(InputStream in, int size) {
	PDDocument document = null;
	try {
	    document = PDDocument.load(in);
	    BufferedImage tmpImage = writeImageFirstPage(document,
		    BufferedImage.TYPE_INT_RGB, size);
	    return createFileFromImage(tmpImage, size);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	} finally {

	    if (document != null) {
		try {
		    document.close();
		} catch (IOException e) {
		}
	    }
	}

    }

    private static File createFileFromImage(BufferedImage tmpImage, int size) {
	try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
	    ImageIO.write(tmpImage, "jpeg", os);
	    if (tmpImage.getWidth() != size) {
		return createThumbnail(tmpImage, os, size);
	    }
	    File outFile = File.createTempFile("data", "pdf");
	    Files.write(os.toByteArray(), outFile);
	    return outFile;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private static File createThumbnail(BufferedImage tmpImage,
	    ByteArrayOutputStream os, int size) {
	try (InputStream is = new ByteArrayInputStream(os.toByteArray())) {
	    return generateThumbnailFromImage(is, size);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private static BufferedImage writeImageFirstPage(PDDocument document,
	    int imageType, int size) throws IOException {
	List<?> pages = document.getDocumentCatalog().getAllPages();
	PDPage page = (PDPage) pages.get(0);
	BufferedImage image = convertToImage(page, imageType, size);
	return image;
    }

    /**
     * 
     * @param page
     * @param imageType
     * @param size
     * @return
     * @throws IOException
     */
    private static BufferedImage convertToImage(PDPage page, int imageType,
	    int size) throws IOException {
	PDRectangle mBox = page.findMediaBox();
	float widthPt = mBox.getWidth();
	float heightPt = mBox.getHeight();
	int widthPx = size;
	float scaling = size / widthPt;
	int heightPx = Math.round(heightPt * scaling);
	Dimension pageDimension = new Dimension((int) widthPt, (int) heightPt);

	BufferedImage retval = new BufferedImage(widthPx, heightPx, imageType);
	Graphics2D graphics = (Graphics2D) retval.getGraphics();
	graphics.setBackground(TRANSPARENT_WHITE);
	graphics.clearRect(0, 0, retval.getWidth(), retval.getHeight());
	graphics.scale(scaling, scaling);
	PageDrawer drawer = new PageDrawer();
	drawer.drawPage(graphics, page, pageDimension);
	try {
	    int rotation = page.findRotation();
	    if ((rotation == 90) || (rotation == 270)) {
		int w = retval.getWidth();
		int h = retval.getHeight();
		BufferedImage rotatedImg = new BufferedImage(w, h,
			retval.getType());
		Graphics2D g = rotatedImg.createGraphics();
		g.rotate(Math.toRadians(rotation), w / 2, h / 2);
		g.drawImage(retval, null, 0, 0);
	    }
	} catch (ImagingOpException e) {
	    play.Logger.warn("can not rotate", e);
	}
	return retval;
    }

    static File generateThumbnailFromImage(InputStream in, int size) {
	File output;
	try {
	    output = File.createTempFile("data", "img");
	    BufferedImage thumbnail = Scalr.resize(ImageIO.read(in), size);
	    ImageIO.write(thumbnail, "jpeg", output);
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	return output;
    }
}
