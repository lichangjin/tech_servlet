package cn.tech.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.tech.common.exception.TechException;
import cn.tech.common.util.TechUtil;
import cn.tech.servlet.handler.OpenHandler;
import cn.tech.servlet.model.HttpResult;
import cn.tech.servlet.model.MethodType;

public class TechDispatchServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory
			.getLog(TechDispatchServlet.class);

	private static final String encoding_utf8 = "utf-8";
	private static final String compare_json = ".json";
	private static final String contentType_json = "application/json";
	private static final String contentType_text = "text/plain;charset=utf-8";

	private static final String header_access_control = "Access-Control-Allow-Origin";

	@Override
	public void init() {
		String webRoot = getServletContext().getRealPath("/WEB-INF");
		TechUtil.setAppRoot(webRoot);
		TechUtil.initLog4j();	
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp, MethodType.get);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp, MethodType.post);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp, MethodType.put);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp, MethodType.delete);
	}

	@SuppressWarnings("unchecked")
	private void processRequest(HttpServletRequest req,
			HttpServletResponse resp, MethodType method) {
		HttpResult<Object> result = new HttpResult<Object>();

		resp.setCharacterEncoding(encoding_utf8);
		resp.setHeader(header_access_control, "*");

		String ext = TechUtil.getFileExtName(req.getRequestURI());
		if (TechUtil.stringCompare(ext, compare_json))
			resp.setContentType(contentType_json);
		else
			resp.setContentType(contentType_text);

		Object obj;
		try {
			obj = OpenHandler.getInstance().handler(req, resp, method, ext);
			if (obj instanceof HttpResult) {
				result = (HttpResult<Object>) obj;
			} else {
				result.setObj(obj);
			}
		} catch (TechException e) {
			result.setCode(String.valueOf(e.getErrorCode()));
			result.setMsg(e.getMessage());
		}

		String jsonString = TechUtil.toJson(result);
		if (jsonString == null)
			jsonString = "{\"code\":-1,\"msg\":\"\", \"obj\":null}";

		PrintWriter writer = null;
		try {
			writer = resp.getWriter();
			resp.setContentLength(jsonString.getBytes().length);
			writer.write(jsonString);
		} catch (Exception e) {
			logger.error(null, e);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

}