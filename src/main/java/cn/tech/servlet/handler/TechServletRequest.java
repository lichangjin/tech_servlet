package cn.tech.servlet.handler;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.tech.common.exception.TechErrorCode;
import cn.tech.common.exception.TechException;
import cn.tech.common.util.TechUtil;

public class TechServletRequest {

	private static final Log logger = LogFactory.getLog(TechServletRequest.class);

	private HttpServletRequest req;
	private long uid = 0L;

	private TechServletRequest(HttpServletRequest req) {
		this.req = req;
	}

	public static TechServletRequest build(HttpServletRequest req) {
		return new TechServletRequest(req);
	}

	public String getUuid() throws TechException {
		String uuid = getString("uuid");
		if (TechUtil.isUuid(uuid))
			return uuid;
		throw new TechException(TechErrorCode.PARAMETER_ERROR, "uuid is err");
	}

	public String getAppid() throws TechException {
		String appid = getString("appid");
		return appid;
	}

	public String getPlatform() throws TechException {
		String platform = getString("platform");
		return platform;
	}

	public String getToken() throws TechException {
		String token = getString("token");
		return token;
	}
	
	
	public long getUId() throws TechException {
		String uuid = getString("uuid");
		int appid = getInt("appid", 0);
		String platform = getString("platform");
		String token = getString("token");
		
		if ((TechUtil.stringIsNullOrEmpty(uuid)) || (uuid.length() > 32) || (!"0".equals(appid)) || (TechUtil.stringIsNullOrEmpty(platform)) || (TechUtil.stringIsNullOrEmpty(token)) || (token.length() < 40)) {
			logger.error("param check error, uuid = " + uuid + ", token = " + token);
			throw new TechException(1003);
		}
		return uid;
	}

	public final boolean isLogin() {
		try {
			return getUId() > 0;
		} catch (TechException e) {
			return false;
		}
	}

	public final HttpServletRequest getHttpServletRequest() {
		return req;
	}

	public final int getInt(String paramName, int defaultValue) {
		return TechUtil.toInt(req.getParameter(paramName), defaultValue);
	}

	public final long getLong(String paramName, long defaultValue) {
		return TechUtil.toLong(req.getParameter(paramName), defaultValue);
	}

	public final String getString(String paramName) {
		return req.getParameter(paramName);
	}

	public final String getString(String paramName, String defaultValue) {
		String value = getString(paramName);
		return value == null ? defaultValue : value;
	}

}