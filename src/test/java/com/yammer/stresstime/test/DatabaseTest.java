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

    private SessionFactory mSessionFactory;
    private Session mSession;

    public DatabaseTest() {
        StresstimeApplication app = RULE.getApplication();
        mSessionFactory = app.getSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return mSessionFactory;
    }

    public Session currentSession() {
        return mSessionFactory.getCurrentSession();
    }

    public void refresh(Object... entities) {
        currentSession().flush();
        for (Object entity : entities) {
            currentSession().refresh(entity);
        }
    }

    @Before
    public void setUp() throws Exception {
        mSession = mSessionFactory.openSession();
        ManagedSessionContext.bind(mSession);
    }

    @After
    public void tearDown() throws Exception {
        mSession.flush();
        mSession.close();
        ManagedSessionContext.unbind(mSessionFactory);
    }
}
