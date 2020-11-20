package server;

import com.github.jeffdoubleyou.signedrequest4j.HttpMethod;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OptionsHandler extends BasicHandler {

	@Override
	public void handle(String target,
	                   Request baseRequest,
	                   HttpServletRequest request,
	                   HttpServletResponse response) {
		try {
			_handle(request.getMethod() == HttpMethod.OPTIONS.name(), HttpMethod.OPTIONS, baseRequest, request, response);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
