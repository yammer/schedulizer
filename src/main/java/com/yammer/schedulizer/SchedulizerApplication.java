package com.yammer.schedulizer;

import com.sun.jersey.api.client.Client;
import com.yammer.schedulizer.auth.*;
import com.yammer.schedulizer.config.SchedulizerConfiguration;
import com.yammer.schedulizer.entities.*;
import com.yammer.schedulizer.managers.*;
import com.yammer.schedulizer.resources.*;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.SessionFactory;

public class SchedulizerApplication extends Application<SchedulizerConfiguration> {

    private static final HibernateBundle<SchedulizerConfiguration> HIBERNATE_BUNDLE = new SchedulizerHibernateBundle(
            AssignableDay.class,
            Assignment.class,
            AssignmentType.class,
            DayRestriction.class,
            Employee.class,
            Group.class,
            Membership.class,
            User.class);

    private UserManager userManager;
    private GroupManager groupManager;
    private EmployeeManager employeeManager;
    private MembershipManager membershipManager;
    private AssignmentTypeManager assignmentTypeManager;
    private AssignmentManager assignmentManager;
    private AssignableDayManager assignableDayManager;
    private DayRestrictionManager dayRestrictionManager;
    private ExtAppAuthenticator extAppAuthenticator;

    private ExtAppType extAppType;

    public static void main(String[] args) throws Exception {
        new SchedulizerApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<SchedulizerConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
        bootstrap.addBundle(HIBERNATE_BUNDLE);
    }

    public String getName() {
        return "schedulizer";
    }

    protected void registerResources(SchedulizerConfiguration config, Environment env) {
        env.jersey().setUrlPattern(config.getRootPath());
        env.jersey().register(new GroupsResource(groupManager));
        env.jersey().register(new GroupDayRestrictionsResource(groupManager, dayRestrictionManager));
        env.jersey().register(new GroupEmployeesResource(employeeManager, groupManager, membershipManager));
        env.jersey().register(new GlobalAdminsResource(employeeManager));
        env.jersey().register(new EmployeesResource(employeeManager));
        env.jersey().register(new EmployeeAssignmentsResource(employeeManager, assignmentManager));
        env.jersey().register(new AssignmentTypesResource(assignmentTypeManager, groupManager));
        env.jersey().register(new AssignmentsResource(assignmentManager, groupManager, employeeManager,
                assignmentTypeManager, assignableDayManager));
        env.jersey().register(new AuthorizationResource(extAppType));
        env.jersey().register(new AdminsResource(groupManager, membershipManager));
        env.jersey().register(new DayRestrictionsResource(employeeManager, dayRestrictionManager));
    }

    protected void registerAuthenticator(SchedulizerConfiguration config, Environment env) {
        Client client = new JerseyClientBuilder(env)
                .using(config.getJerseyClientConfiguration())
                .build(getName());
        extAppAuthenticator = ExtAppAuthenticatorFactory.getExtAppAuthenticator(extAppType, client);
        AbstractAuthenticator authenticator = new Authenticator(client, userManager, employeeManager, extAppAuthenticator,
                ExtAppType.valueOf(config.getExtApp()));
        env.jersey().register(new AuthorizeProvider<>(authenticator));
    }

    @Override
    public void run(SchedulizerConfiguration config, Environment env) throws Exception {
        SessionFactory sessionFactory = HIBERNATE_BUNDLE.getSessionFactory();
        userManager = new UserManager(sessionFactory);
        groupManager = new GroupManager(sessionFactory);
        employeeManager = new EmployeeManager(sessionFactory);
        membershipManager = new MembershipManager(sessionFactory);
        assignmentTypeManager = new AssignmentTypeManager(sessionFactory);
        assignmentManager = new AssignmentManager(sessionFactory);
        assignableDayManager = new AssignableDayManager(sessionFactory);
        dayRestrictionManager = new DayRestrictionManager(sessionFactory);

        extAppType = ExtAppType.valueOf(config.getExtApp());

        registerResources(config, env);
        registerAuthenticator(config, env);
    }

    // For tests
    public SessionFactory getSessionFactory() {
        return HIBERNATE_BUNDLE.getSessionFactory();
    }

    private static class SchedulizerHibernateBundle extends HibernateBundle<SchedulizerConfiguration> {

        protected SchedulizerHibernateBundle(Class<?> entity, Class<?>... entities) {
            super(entity, entities);
        }

        @Override
        public DataSourceFactory getDataSourceFactory(SchedulizerConfiguration config) {
            return config.getDataSourceFactory();
        }
    }
}
