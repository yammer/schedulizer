package com.yammer.stresstime;

import com.yammer.stresstime.config.StresstimeConfiguration;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.managers.*;
import com.yammer.stresstime.resources.*;
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

    private GroupManager groupManager;
    private EmployeeManager employeeManager;
    private MembershipManager membershipManager;
    private AssignmentTypeManager assignmentTypeManager;
    private AssignmentManager assignmentManager;
    private AssignableDayManager assignableDayManager;

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
        SessionFactory sessionFactory = HIBERNATE_BUNDLE.getSessionFactory();
        groupManager = new GroupManager(sessionFactory);
        employeeManager = new EmployeeManager(sessionFactory);
        membershipManager = new MembershipManager(sessionFactory);
        assignmentTypeManager = new AssignmentTypeManager(sessionFactory);
        assignmentManager = new AssignmentManager(sessionFactory);
        assignableDayManager = new AssignableDayManager(sessionFactory);

        env.jersey().setUrlPattern(config.getRootPath());
        env.jersey().register(new GroupsResource(groupManager));
        env.jersey().register(new GroupEmployeesResource(employeeManager, groupManager, membershipManager));
        env.jersey().register(new EmployeesResource(employeeManager));
        env.jersey().register(new AssignmentTypesResource(assignmentTypeManager, groupManager));
        env.jersey().register(new AssignmentsResource(assignmentManager, groupManager, employeeManager,
                assignmentTypeManager, assignableDayManager));
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
