package com.yammer.stresstime.test;

import com.yammer.stresstime.StresstimeApplication;
import com.yammer.stresstime.config.StresstimeConfiguration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;

/* TODO: Fix problem when running all tests at once, probably related to RULE being loaded once across suites */
public class DatabaseTest {

    @ClassRule
    public static final DropwizardAppRule<StresstimeConfiguration> RULE =
            new DropwizardAppRule<>(StresstimeApplication.class, "app.yml");

    private SessionFactory sessionFactory;
    private Session session;
    private boolean flushSession;

    public DatabaseTest() {
        StresstimeApplication app = RULE.getApplication();
        sessionFactory = app.getSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

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

    @After
    public void tearDown() throws Exception {
        if (flushSession) {
            session.flush();
        }
        session.close();
        ManagedSessionContext.unbind(sessionFactory);
    }
}
