package config.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import dao.PixivDao;

public class CommonGuiceModule extends AbstractModule {
	
	@Override
    public void configure() {
        bind(PixivDao.class).in(Singleton.class);
    }
}