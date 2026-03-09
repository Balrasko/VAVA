package dev.vavateam1;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import dev.vavateam1.dao.UserDao;
import dev.vavateam1.dao.UserDaoImpl;
import dev.vavateam1.dao.UserSessionDao;
import dev.vavateam1.dao.UserSessionDaoImpl;
import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.service.AuthService;
import dev.vavateam1.service.LocalAuthService;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AuthService.class).to(LocalAuthService.class).in(Scopes.SINGLETON);
        bind(ConnectionFactory.class).in(Scopes.SINGLETON);
        bind(UserDao.class).to(UserDaoImpl.class).in(Scopes.SINGLETON);
        bind(UserSessionDao.class).to(UserSessionDaoImpl.class).in(Scopes.SINGLETON);
    }
}
