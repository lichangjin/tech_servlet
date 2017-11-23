package cn.tech.servlet.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.tech.common.exception.TechErrorCode;
import cn.tech.common.exception.TechException;
import cn.tech.common.util.TechUtil;
import cn.tech.servlet.model.MethodType;

public class ServletHandler {

	private static final Log logger = LogFactory.getLog(ServletHandler.class);

	private static final byte[] syncRoot = new byte[0];

	private List<UrlRewrite> mapping = new ArrayList<UrlRewrite>();
	private Map<Class<?>, Object> handlers = new HashMap<>();

	public ServletHandler(String config) throws TechException {
		List<UrlRewrite> temp = new ArrayList<UrlRewrite>();
		String file = TechUtil.getConfigFile(config);
		try {
			File f = new File(file);
			InputStreamReader insReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
			@SuppressWarnings("resource")
			BufferedReader in = new BufferedReader(insReader);
			String line = null;
			while ((line = in.readLine()) != null) {
				if (!TechUtil.stringIsNullOrEmpty(line) && !line.startsWith("#")) {
					String[] s = line.split("=");
					if (s == null || s.length != 2)
						throw new RuntimeErrorException(null, line + " is error(=).");
					UrlRewrite item = new UrlRewrite();
					item.setPattern(Pattern.compile(s[0]));
					String[] cm = s[1].split(":");
					if (cm == null || cm.length < 2)
						throw new RuntimeErrorException(null, line + " is error(=).");
					Class<?> clazz = Class.forName(cm[0]);
					item.setClazz(clazz);
					Method[] methods = clazz.getMethods();
					for (Method m : methods) {
						if (TechUtil.stringCompare(m.getName(), cm[1]))
							item.addMethod(m);
					}
					temp.add(item);
					if (cm.length > 2)
						item.setCache_switch(TechUtil.toInt(cm[2], 0) > 0);
					if (cm.length > 3)
						item.setCache_time(TechUtil.toInt(cm[3], 0));
					if (cm.length > 4)
						item.setCheck_login(TechUtil.toInt(cm[4], 0) > 0);

					if (item.getMethods().isEmpty())
						throw new RuntimeErrorException(null, line + " is error(=).");
				}
			}
			mapping = temp;
		} catch (IOException e) {
			throw new TechException(TechErrorCode.UNDEFINED_ERROR, e);
		} catch (ClassNotFoundException e) {
			throw new TechException(TechErrorCode.UNDEFINED_ERROR, e);
		}
	}

	public Object handler(HttpServletRequest req, HttpServletResponse resp, MethodType methodType, String ext) throws TechException {
		try {
			String url = req.getRequestURI();
			if (ext == null)
				ext = TechUtil.getFileExtName(url);

			String[] paths = null;
			String u = null;
			if (!TechUtil.stringIsNullOrEmpty(url)) {
				while (url.indexOf("//") > 0)
					url = url.replaceAll("//", "/");
				u = methodType.name() + " " + url;
				while (url.startsWith("/"))
					url = url.substring(1);
				if (url.endsWith(ext))
					url = url.substring(0, url.length() - ext.length());

				paths = url.toLowerCase().split("/");
			} else
				u = methodType.name() + " /";

			for (UrlRewrite item : mapping) {
				Matcher m = item.getPattern().matcher(u);
				if (m.find()) {
					for (Method method : item.getMethods()) {
						Class<?>[] paramTypes = method.getParameterTypes();
						if (paramTypes.length != 5)
							continue;
						if (!paramTypes[0].getName().equals(TechServletRequest.class.getName()))
							continue;
						if (!paramTypes[1].getName().equals(HttpServletResponse.class.getName()))
							continue;
						if (!paramTypes[2].getName().equals(methodType.getClass().getName()))
							continue;
						if (!paramTypes[3].getName().equals(paths.getClass().getName()))
							continue;
						if (!paramTypes[4].getName().equals(ext.getClass().getName()))
							continue;
						return method.invoke(getHandler(item.getClazz()), new Object[] { TechServletRequest.build(req), resp, methodType, paths, ext });
					}
				}
			}
			throw new TechException(TechErrorCode.UNDEFINED_ERROR, "url_write(" + u + ") is out of the config.");
		} catch (TechException e) {
			throw e;
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if (t instanceof TechException)
				throw (TechException) t;
			logger.error(null, e);
			throw new TechException(TechErrorCode.UNDEFINED_ERROR);
		} catch (Exception e) {
			logger.error(null, e);
			throw new TechException(TechErrorCode.UNDEFINED_ERROR);
		}
	}

	public Object handler(String url, MethodType methodType, String ext) {
		try {
			boolean is_run = false;
			if (ext == null)
				ext = TechUtil.getFileExtName(url);

			String[] paths = null;
			String u = null;
			if (!TechUtil.stringIsNullOrEmpty(url)) {
				while (url.indexOf("//") > 0)
					url = url.replaceAll("//", "/");
				u = methodType.name() + " " + url;
				while (url.startsWith("/"))
					url = url.substring(1);
				if (url.endsWith(ext))
					url = url.substring(0, url.length() - ext.length());

				paths = url.toLowerCase().split("/");
			} else
				u = methodType.name() + " /";

			for (UrlRewrite item : mapping) {
				Matcher m = item.getPattern().matcher(u);
				if (m.find()) {
					is_run = true;
					// return item.getMethod().invoke(null, new Object[] { null,
					// null, method, paths, ext });

					for (Method method : item.getMethods()) {
						Class<?>[] paramTypes = method.getParameterTypes();
						if (paramTypes.length != 3)
							continue;
						if (!paramTypes[0].getName().equals(methodType.getClass().getName()))
							continue;
						if (!paramTypes[1].getName().equals(paths.getClass().getName()))
							continue;
						if (!paramTypes[2].getName().equals(ext.getClass().getName()))
							continue;
						return method.invoke(getHandler(item.getClazz()), new Object[] { methodType, paths, ext });
					}
					is_run = false;
				}
			}
			if (!is_run)
				throw new TechException(TechErrorCode.UNDEFINED_ERROR, "url_write(" + u + ") is out of the config.");
		} catch (Exception e) {
			logger.error(null, e);
		}
		return null;
	}

	private Object getHandler(Class<?> clazz) throws TechException {
		Object handler = handlers.get(clazz);
		if (handler == null) {
			synchronized (syncRoot) {
				if (handler == null) {
					try {
						handler = clazz.newInstance();
						handlers.put(clazz, handler);
					} catch (InstantiationException e) {
						throw TechException.throwTechException(e);
					} catch (IllegalAccessException e) {
						throw TechException.throwTechException(e);
					}
				}
			}
		}
		return handler;
	}

	class UrlRewrite {

		private Pattern pattern;
		private List<Method> methods = new ArrayList<Method>();
		private Class<?> clazz;
		private int cache_time = 60;
		private boolean cache_switch = false;
		private boolean check_login = false;

		public boolean isCheck_login() {
			return check_login;
		}

		public void setCheck_login(boolean check_login) {
			this.check_login = check_login;
		}

		public boolean getCache_switch() {
			return cache_switch && cache_time > 0;
		}

		public void setCache_switch(boolean cache_switch) {
			this.cache_switch = cache_switch;
		}

		public int getCache_time() {
			return cache_time;
		}

		public void setCache_time(int sec) {
			this.cache_time = sec;
		}

		public Pattern getPattern() {
			return pattern;
		}

		public void setPattern(Pattern pattern) {
			this.pattern = pattern;
		}

		public List<Method> getMethods() {
			return methods;
		}

		public void setMethods(List<Method> methods) {
			this.methods = methods;
		}

		public void addMethod(Method method) {
			if (methods == null)
				methods = new ArrayList<>();
			this.methods.add(method);
		}

		public Class<?> getClazz() {
			return clazz;
		}

		public void setClazz(Class<?> clazz) {
			this.clazz = clazz;
		}

	}
}