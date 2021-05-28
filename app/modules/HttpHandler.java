package modules;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.api.http.JavaCompatibleHttpRequestHandler;
import play.http.DefaultHttpRequestHandler;
import play.http.HandlerForRequest;
import play.mvc.Http.RequestHeader;

public class HttpHandler extends DefaultHttpRequestHandler{
    
    private final ch.qos.logback.classic.Logger logger = 
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(this.getClass());

    public HttpHandler(JavaCompatibleHttpRequestHandler underlying) {
        super(underlying);
        // TODO Auto-generated constructor stub
    }   
    
    
    @Override
    public HandlerForRequest handlerForRequest(RequestHeader request) {
        logger.debug(/*"\n" + request.toString() + "\n\t" + mapToString(request.getHeaders().toString()) + "\n\t"
                + request.body().toString()*/"OnRequest Method comes here");
        return null;
    }
    
    /*
    private String mapToString(Map<String, String[]> map) {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, String[]>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String[]> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=').append('"');
            sb.append(Arrays.toString(entry.getValue()));
            sb.append('"');
            if (iter.hasNext()) {
                sb.append("\n\t'");
            }
        }
        return sb.toString();

    }
*/

}
