package org.layr.commons;

import java.lang.reflect.Type;

public interface IConverter {

	Object decode(Object value, Class<?> type, Type[] genericTypes);
	String encode(Object object);

}
