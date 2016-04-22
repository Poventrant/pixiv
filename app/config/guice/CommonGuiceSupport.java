package config.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class CommonGuiceSupport {
	
	private static Injector in = null;

	public static Injector getInjector(){
		return in;
	}
	
	public static Injector setupInjector(){
		CommonGuiceModule model = new CommonGuiceModule();
		in = Guice.createInjector(model);
		return in;
	}
	
}
