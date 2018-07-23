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

import static helper.Globals.*;
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.imgscalr.Scalr;

import com.google.common.io.Files;
import com.google.common.net.MediaType;

import play.Play;

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
    
    /**
     * @param ts
     *            the actual content to create a thumbnail from
     * @param contentType
     *            the MediaType for the content
     * @param size
     *            acually the width
     * @param name 
     * @return a thumbnail file
     */
    public static File createThumbnail(InputStream ts, MediaType contentType, int size, String name) {
        File result = null;
        try {

            if (contentType.is(MediaType.JPEG)) {
                result = generateThumbnailFromImage(ts, size, "jpeg",name);
            } else if (contentType.is(MediaType.PNG)) {
                result = generateThumbnailFromImage(ts, size, "png",name);
            }else if (contentType.is(MediaType.GIF)) {
                result = generateThumbnailFromImage(ts, size, "gif",name);
            } else if (contentType.is(MediaType.PDF)) {
                result = generateThumbnailFromPdf(ts, size,name);
            } else {
                result = generateMimeTypeImage(contentType, size,name);
            }
        } catch (Exception e) {
            play.Logger.warn("", e);
            result = generateThumbnailFromImage(Play.application().resourceAsStream(THUMBNAIL_EXCEPTION_PIC),
                    size, "png",name);
        }
        return result;
    }

    private static File generateMimeTypeImage(MediaType contentType, int size,String name) {
        File result = null;
        try {

            if (contentType.is(MediaType.ANY_AUDIO_TYPE)) {
                result = generateThumbnailFromImage(Play.application().resourceAsStream(AUDIO_PIC),
                        size, "png",name);
            } else if (contentType.is(MediaType.ANY_IMAGE_TYPE)) {
                result = generateThumbnailFromImage(Play.application().resourceAsStream(IMAGE_PIC),
                        size, "png",name);
            } else if (contentType.is(MediaType.ANY_TEXT_TYPE) || contentType.is(MediaType.OOXML_DOCUMENT) || contentType.is(MediaType.MICROSOFT_WORD)) {
                result = generateThumbnailFromImage(Play.application().resourceAsStream(TEXT_PIC), size,
                        "png",name);
            } else if (contentType.is(MediaType.ANY_VIDEO_TYPE)) {
                result = generateThumbnailFromImage(Play.application().resourceAsStream(VIDEO_PIC),
                        size, "png",name);
            } else if (contentType.is(MediaType.ZIP)) {
                result = generateThumbnailFromImage(Play.application().resourceAsStream(ZIP_PIC), size,
                        "png",name);
            } else if (contentType.is(MediaType.PDF)) {
                result = generateThumbnailFromImage(Play.application().resourceAsStream(PDF_PIC), size,
                        "png",name);
            } else {
                result = generateThumbnailFromImage(
                        Play.application().resourceAsStream(MIMETYPE_NOT_FOUND_PIC), size, "png",name);
            }
        } catch (Exception e) {
            play.Logger.warn("", e);
            result = generateThumbnailFromImage(Play.application().resourceAsStream(EXCEPTION_ON_APPLY_MIMETYPE),
                    size, "png",name);
        }
        return result;
    }

    private static File generateThumbnailFromPdf(InputStream in, int size,String name) {
        PDDocument document = null;
        try {
            document = PDDocument.load(in);
            BufferedImage tmpImage = writeImageFirstPage(document, BufferedImage.TYPE_INT_RGB, size);
            return createFileFromImage(tmpImage, size,name);
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

    private static File createFileFromImage(BufferedImage tmpImage, int size,String name) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(tmpImage, "jpeg", os);
            if (tmpImage.getWidth() != size) {
                return createThumbnail(tmpImage, os, size,name);
            }
            File outFile = File.createTempFile("data", "pdf");
            Files.write(os.toByteArray(), outFile);
            return outFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static File createThumbnail(BufferedImage tmpImage, ByteArrayOutputStream os, int size,String name) {
        try (InputStream is = new ByteArrayInputStream(os.toByteArray())) {
            return generateThumbnailFromImage(is, size, "jpeg",name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage writeImageFirstPage(PDDocument document, int imageType, int size) throws IOException {
        PDDocumentCatalog dc = document.getDocumentCatalog();
        dc.setPageMode(PDDocumentCatalog.PAGE_MODE_USE_THUMBS);
        dc.setPageLayout(PDDocumentCatalog.PAGE_LAYOUT_SINGLE_PAGE);

        PDPage page = (PDPage) dc.getAllPages().get(0);

        BufferedImage image = page.convertToImage(imageType, size);
        return image;
    }

    private static File generateThumbnailFromImage(InputStream ts, int size, String imageType, String name) {
        File output;
        try {
            output = File.createTempFile(name+"-thumby","test");
            BufferedImage thumbnail = Scalr.resize(ImageIO.read(ts), size);
            ImageIO.write(thumbnail, imageType, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output;
    }

}
