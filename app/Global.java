
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
import static play.mvc.Results.notFound;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import play.Application;
import play.GlobalSettings;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Request;
import play.mvc.Http.RequestHeader;
import play.mvc.SimpleResult;

/**
 * @author Jan Schnasse
 *
 */
public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        play.Logger.info("Application has started");

    }

    @Override
    public void onStop(Application app) {
        // Globals.profile.saveMap();
        play.Logger.info("Application shutdown...");
    }

    public Promise<SimpleResult> onHandlerNotFound(RequestHeader request) {
        return Promise.<SimpleResult> pure(notFound("Action not found " + request.uri()));
    }

    @SuppressWarnings("rawtypes")
    public Action onRequest(Request request, Method actionMethod) {
        play.Logger.debug("\n" + request.toString() + "\n\t" + mapToString(request.headers()) + "\n\t"
                + request.body().toString());
        return super.onRequest(request, actionMethod);
    }

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
}
