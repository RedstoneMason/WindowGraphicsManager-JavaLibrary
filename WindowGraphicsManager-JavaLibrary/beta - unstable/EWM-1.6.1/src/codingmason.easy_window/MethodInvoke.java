package codingmason.easy_window;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class MethodInvoke {
	private boolean printErrors = true;
	private Object parent;
	private String path;
	private Method method;
	private Class<?>[] paramClass;
	private boolean error = false;
	
	// CONSTRUCTOR
	protected MethodInvoke(String path, Object parent, Class<?>... params) {
		this.parent = parent;
		this.path = path;
		this.paramClass = params;
	}
	
	public void printErrors(boolean printErrors) {
		this.printErrors = printErrors;
	}
	
	// PUBLIC
	public void invoke(Object... params) {
		if(error) return;
		try {
			if(method == null) {
				String className = path.substring(0, path.lastIndexOf("."));
				String methodName = path.substring(path.lastIndexOf(".") + 1);
				Class<?> c = Class.forName(className);
				method = c.getMethod(methodName, paramClass);
			}
			method.invoke(parent, params);
		} catch (SecurityException | IllegalAccessException	| IllegalArgumentException |  
				ClassNotFoundException | NoSuchMethodException | StringIndexOutOfBoundsException | NullPointerException e) {
			error = true;
			if(printErrors) e.printStackTrace();
		} catch(InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}