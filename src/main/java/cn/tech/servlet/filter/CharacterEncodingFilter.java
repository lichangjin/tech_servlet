package cn.tech.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class CharacterEncodingFilter implements Filter {
	
	protected String encoding = "utf-8";
	protected FilterConfig filterConfig = null;
	protected boolean ignore = true;

	protected String selectEncoding(ServletRequest request) {
		return this.encoding;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		String encoding = selectEncoding(request);
		if (encoding != null)
			request.setCharacterEncoding(encoding);

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.filterConfig = config;
		this.encoding = filterConfig.getInitParameter("encoding");
	}

	public void destroy() {
		this.encoding = null;
		this.filterConfig = null;
	}
}
