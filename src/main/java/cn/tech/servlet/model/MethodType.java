package cn.tech.servlet.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum MethodType implements Serializable {
	
	get(1), post(2), put(3), delete(4);

	MethodType(int value) {
		this.value = value;
	}

	int value = 0;

	public int value() {
		return this.value;
	}

	private static Map<Integer, MethodType> map = new HashMap<Integer, MethodType>();

	static {
		for (MethodType num : MethodType.values()) {
			map.put(new Integer(num.value()), num);
		}
	}

	public static MethodType lookup(int value) {
		return map.get(Integer.valueOf(value));
	}

}
