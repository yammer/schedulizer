package com.yammer.stresstime.test;

import com.yammer.stresstime.StresstimeApplication;
import com.yammer.stresstime.TestSuite;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.After;
import org.junit.Before;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/* TODO: Extract this class into a rule (allowing test classes to subclass other classes) and create an annotation
 * TODO: for tests that want to be wrapped in a hibernate session, allowing classes to have db test methods and
 * TODO: non-db test methods */
public class DatabaseTest {

    private static SessionFactory sessionFactory;
    private Session session;
    private boolean flushSession;
    private static StresstimeApplication app;

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

    public StresstimeApplication getApp() { return app; }

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
        session.getTransaction().begin();
        ManagedSessionContext.bind(session);
        flushSession = true;
    }

    @After
    public void tearDown() throws Exception {
        if (flushSession) {
            session.flush();
        }
        session.getTransaction().rollback();
        session.close();
        ManagedSessionContext.unbind(sessionFactory);
    }

}
