package com.yammer.stresstime;

import com.yammer.stresstime.config.StresstimeConfiguration;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.resources.TestResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.SessionFactory;

public class StresstimeApplication extends Application<StresstimeConfiguration> {

    private static final HibernateBundle<StresstimeConfiguration> HIBERNATE_BUNDLE = new StresstimeHibernateBundle(
            AssignableDay.class,
            Assignment.class,
            AssignmentType.class,
            DayRestriction.class,
            Employee.class,
            Group.class,
            Membership.class);

    private GroupManager mGroupManager;

    public static void main(String[] args) throws Exception {
        new StresstimeApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<StresstimeConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
        bootstrap.addBundle(HIBERNATE_BUNDLE);
    }

    public String getName() {
        return "stresstime";
    }

    @Override
    public void run(StresstimeConfiguration config, Environment env) throws Exception {
        mGroupManager = new GroupManager(HIBERNATE_BUNDLE.getSessionFactory());

        env.jersey().setUrlPattern(config.getRootPath());
        env.jersey().register(new TestResource(mGroupManager));
    }

    // For tests
    public SessionFactory getSessionFactory() {
        return HIBERNATE_BUNDLE.getSessionFactory();
    }

    private static class StresstimeHibernateBundle extends HibernateBundle<StresstimeConfiguration> {

        protected StresstimeHibernateBundle(Class<?> entity, Class<?>... entities) {
            super(entity, entities);
        }

        @Override
        public DataSourceFactory getDataSourceFactory(StresstimeConfiguration config) {
            return config.getDataSourceFactory();
        }
    }
}
