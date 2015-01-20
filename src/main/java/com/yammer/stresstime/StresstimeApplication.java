package com.yammer.stresstime;

import com.yammer.stresstime.config.StresstimeConfiguration;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.managers.AssignmentTypeManager;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.managers.MembershipManager;
import com.yammer.stresstime.resources.AssignmentTypesResource;
import com.yammer.stresstime.resources.EmployeesResource;
import com.yammer.stresstime.resources.GroupEmployeesResource;
import com.yammer.stresstime.resources.GroupsResource;
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
    private EmployeeManager mEmployeeManager;
    private MembershipManager mMembershipManager;
    private AssignmentTypeManager mAssignmentTypeManager;

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
        mEmployeeManager = new EmployeeManager(HIBERNATE_BUNDLE.getSessionFactory());
        mMembershipManager = new MembershipManager(HIBERNATE_BUNDLE.getSessionFactory());
        mAssignmentTypeManager = new AssignmentTypeManager(HIBERNATE_BUNDLE.getSessionFactory());

        env.jersey().setUrlPattern(config.getRootPath());
        env.jersey().register(new GroupsResource(mGroupManager));
        env.jersey().register(new EmployeesResource(mEmployeeManager, mGroupManager, mMembershipManager));
        env.jersey().register(new GroupEmployeesResource(mEmployeeManager));
        env.jersey().register(new AssignmentTypesResource(mAssignmentTypeManager, mGroupManager));
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
