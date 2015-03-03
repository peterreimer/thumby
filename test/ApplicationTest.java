import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import models.Thumbnail;

import org.junit.Test;

import play.mvc.Content;

/**
 *
 * Simple (JUnit) tests that can call all parts of a Play app. If you are
 * interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest {

    @Test
    public void simpleCheck() {
	int a = 1 + 1;
	assertThat(a).isEqualTo(2);
    }

    @Test
    public void indexTemplateShouldContainTheStringThatIsPassedToIt() {
	running(fakeApplication(), new Runnable() {
	    public void run() {
		Content html = views.html.index.render(new Thumbnail());
		assertThat(contentType(html)).isEqualTo("text/html");
		assertThat(contentAsString(html)).contains(
			"Supports PDF and Images.");
	    }
	});
    }
}
