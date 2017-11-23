package cn.tech.servlet.handler;

import cn.tech.common.exception.TechException;

public class OpenHandler extends ServletHandler {

	private static final byte[] syncRoot = new byte[0];
	private static OpenHandler instance;

	public OpenHandler(String config) throws TechException {
		super(config);
	}

	public static OpenHandler getInstance() throws TechException {
		if (instance == null) {
			synchronized (syncRoot) {
				if (instance == null)
					instance = new OpenHandler("url_rewrite.properties");
			}
		}
		return instance;
	}
		
}