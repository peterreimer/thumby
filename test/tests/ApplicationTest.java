package tests;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.io.File;
import java.io.FileInputStream;

import models.Thumbnail;

import org.junit.Test;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.net.MediaType;

import play.Play;
import play.mvc.Content;

/**
 *
 * Simple (JUnit) tests that can call all parts of a Play app. If you are
 * interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest {

    private static final String HASH_OF_DEFAULT_DOWNLOAD_PIC = "0fa2e753d9c34bc42064585e96bed4fd";

    @Test
    public void test_thumbnailer() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                 org.junit.Assert.assertEquals("63ae2f7551277468be0f416c9267030d",createTestThumbnail("test.jpg",MediaType.JPEG));
                 org.junit.Assert.assertEquals("a1c75531df7039c6515b17e5378b8bb5",createTestThumbnail("test.png",MediaType.PNG));
                 org.junit.Assert.assertEquals("8c58ffea16b7cab3794221288e66c733",createTestThumbnail("test.zip",MediaType.ZIP));
                 org.junit.Assert.assertEquals(HASH_OF_DEFAULT_DOWNLOAD_PIC,createTestThumbnail("test.ods",MediaType.OPENDOCUMENT_SPREADSHEET));
                 org.junit.Assert.assertEquals("b89f887968762d629c193ae25db02e05",createTestThumbnail("test.pdf", MediaType.PDF));
                 org.junit.Assert.assertEquals("740d6aeff9cd1a6263890cc5e758e332",createTestThumbnail("test.xcf",MediaType.ANY_IMAGE_TYPE));
                 org.junit.Assert.assertEquals("b856b3d7b882f25d0986416ac1aa0102",createTestThumbnail("test.gif",MediaType.GIF));
            }

            private String createTestThumbnail(String name, MediaType mtype) {
                try {
                    Thumbnail t = controllers.Application.createThumbnail(
                            new FileInputStream(
                                    new File(Play.application().resource( name).toURI().getPath())),
                            mtype, 200, name);
                    HashCode hashcode= Files.hash(t.thumb, Hashing.md5());
                    play.Logger.info("Create Test Data at " + t.thumb.getAbsolutePath()+" Hashcode: "+hashcode);
                    return hashcode.toString();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
