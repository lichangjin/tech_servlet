package cn.tech.servlet.model;

import java.io.Serializable;

public class HttpResult<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private String code = "0";
	private String msg = "";
	private T obj;

	public HttpResult() {
		
	}
	
	public HttpResult(String code) {
		this(code, "");
	}

	public HttpResult(String code, String msg) {
		this(code, msg, null);
	}

	public HttpResult(String code, String msg, T obj) {
		this.code = code;
		this.msg = msg;
		this.obj = obj;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getObj() {
		return obj;
	}

	public void setObj(T obj) {
		this.obj = obj;
	}

}