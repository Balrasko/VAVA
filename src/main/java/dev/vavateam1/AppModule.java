package dev.vavateam1;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import dev.vavateam1.dao.LocationDao;
import dev.vavateam1.dao.LocationDaoImpl;
import dev.vavateam1.dao.CategoryDao;
import dev.vavateam1.dao.CategoryDaoImpl;
import dev.vavateam1.dao.MenuItemDao;
import dev.vavateam1.dao.MenuItemDaoImpl;
import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.OrderItemDaoImpl;
import dev.vavateam1.dao.PaymentDao;
import dev.vavateam1.dao.PaymentDaoImpl;
import dev.vavateam1.dao.TableDao;
import dev.vavateam1.dao.TableDaoImpl;
import dev.vavateam1.dao.UserDao;
import dev.vavateam1.dao.UserDaoImpl;
import dev.vavateam1.dao.UserSessionDao;
import dev.vavateam1.dao.UserSessionDaoImpl;
import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.service.*;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AuthService.class).to(LocalAuthService.class).in(Scopes.SINGLETON);
        bind(ConnectionFactory.class).in(Scopes.SINGLETON);
        bind(UserDao.class).to(UserDaoImpl.class).in(Scopes.SINGLETON);
        bind(UserSessionDao.class).to(UserSessionDaoImpl.class).in(Scopes.SINGLETON);
        bind(TableService.class).to(TableServiceImpl.class).in(Scopes.SINGLETON);
        bind(TableDao.class).to(TableDaoImpl.class).in(Scopes.SINGLETON);
        bind(LocationDao.class).to(LocationDaoImpl.class).in(Scopes.SINGLETON);
        bind(CategoryDao.class).to(CategoryDaoImpl.class).in(Scopes.SINGLETON);
        bind(PaymentDao.class).to(PaymentDaoImpl.class).in(Scopes.SINGLETON);
        bind(OrderItemDao.class).to(OrderItemDaoImpl.class).in(Scopes.SINGLETON);
        bind(HistoryService.class).to(HistoryServiceImpl.class).in(Scopes.SINGLETON);
        bind(KitchenService.class).in(Scopes.SINGLETON);
        bind(MenuItemDao.class).to(MenuItemDaoImpl.class).in(Scopes.SINGLETON);
        bind(MenuService.class).to(MenuServiceImpl.class).in(Scopes.SINGLETON);
        bind(OrderService.class).to(OrderServiceImpl.class).in(Scopes.SINGLETON);
    }
}
