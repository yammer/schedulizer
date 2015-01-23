package com.yammer.stresstime.resources;

import com.google.common.base.Objects;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;



public class DatabaseMock {
    private static final SessionFactory mSessionFactory = mock(SessionFactory.class);
    private static final Session mSession = mock(Session.class);

    HashMap<Class, Object> mKnownEntities;
    HashMap<Long, Object> mDatabase;

    public DatabaseMock(List<Object> knownEntities) {
        mKnownEntities = new HashMap<>();
        for (Object o : knownEntities) {
            mKnownEntities.put(o.getClass(), o);
        }
        mDatabase = new HashMap<>();
        when(mSessionFactory.getCurrentSession()).thenReturn(mSession);
        long lastId = 0;
        for(Map.Entry<Class, Object> e : mKnownEntities.entrySet()) {
            final long id = lastId;
            final Map.Entry<Class, Object> entry = e;
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    mDatabase.put(id, entry.getValue());
                    return entry.getValue();
                }
            }).when(mSession).saveOrUpdate(entry.getValue());
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    mDatabase.remove(id);
                    return null;
                }
            }).when(mSession).delete(id);
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return mDatabase.get(id);
                }
            }).when(mSession).get(entry.getKey(), id);
            lastId++;
        }
    }

    public SessionFactory getMock(){
        return mSessionFactory;
    }

    @Before
    public void setup() {

    }
}
