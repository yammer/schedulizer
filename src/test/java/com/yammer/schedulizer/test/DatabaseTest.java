package com.yammer.schedulizer.test;

import com.yammer.schedulizer.SchedulizerApplication;
import com.yammer.schedulizer.TestSuite;
import com.yammer.schedulizer.entities.BaseEntity;
import com.yammer.schedulizer.managers.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;
import java.util.List;

/* TODO: Extract this class into a rule (allowing test classes to subclass other classes) and create an annotation
 * TODO: for tests that want to be wrapped in a hibernate session, allowing classes to have db test methods and
 * TODO: non-db test methods */
public class DatabaseTest {

    private static SessionFactory sessionFactory;
    private Session session;
    private boolean flushSession;
    private static SchedulizerApplication app;

    {{
        app = TestSuite.RULE.getApplication();
        sessionFactory = app.getSessionFactory();
    }}

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    public SchedulizerApplication getApp() { return app; }

    public void refresh(Object... entities) {
        currentSession().flush();
        for (Object entity : entities) {
            currentSession().refresh(entity);
        }
    }

    protected void hibernateThrewException() {
        flushSession = false;
    }

    @Before
    public void setUp() throws Exception {
        session = sessionFactory.openSession();
        ManagedSessionContext.bind(session);
        flushSession = true;
    }

    private List<EntityManager> entityManagers = Arrays.asList(
            new AssignableDayManager(getSessionFactory()),
            new AssignmentManager(getSessionFactory()),
            new AssignmentTypeManager(getSessionFactory()),
            new DayRestrictionManager(getSessionFactory()),
            new EmployeeManager(getSessionFactory()),
            new GroupManager(getSessionFactory()),
            new MembershipManager(getSessionFactory()),
            new UserManager(getSessionFactory()));
    @After
    public void tearDown() throws Exception {
        // Clear database for next test
        for (EntityManager manager : entityManagers) {
            List<BaseEntity> entities = manager.all();
            for (BaseEntity entity : entities) {
                manager.delete(entity);
            }
        }
        if (flushSession) {
            session.flush();
        }
        session.close();
        ManagedSessionContext.unbind(sessionFactory);

    }

}
