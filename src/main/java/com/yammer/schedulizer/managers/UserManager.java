package com.yammer.schedulizer.managers;

import com.yammer.schedulizer.auth.ExtAppAuthenticatorFactory;
import com.yammer.schedulizer.auth.ExtAppType;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.User;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import static com.google.common.base.Preconditions.checkState;

public class UserManager extends EntityManager<User> {

    public UserManager(SessionFactory sessionFactory) {
        super(sessionFactory, User.class);
    }

    public User safeGetByExtAppId(String extAppId) {
        Employee employee = getUnique(currentSession()
                .createCriteria(Employee.class)
                .add(Restrictions.eq("extAppId", extAppId)), Employee.class);
        return employee.getUser();
    }

    @Override
    public void save(User user) {
        checkState(!user.isGuest(), "Cannot save a guest");
        super.save(user);
        currentSession().refresh(user.getEmployee());
    }

    public long count(ExtAppType extAppType) {
        return ((Number) currentSession()
                .createCriteria(User.class)
                .add(Restrictions.eq("extAppType", extAppType.toString()))
                .setProjection(Projections.rowCount())
                .uniqueResult())
                .longValue();
    }
}
