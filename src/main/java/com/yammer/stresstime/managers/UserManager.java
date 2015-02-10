package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.User;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import static com.google.common.base.Preconditions.*;

public class UserManager extends EntityManager<User> {

    public UserManager(SessionFactory sessionFactory) {
        super(sessionFactory, User.class);
    }

    public User safeGetByYammerId(String yammerId) {
        Employee employee = getUnique(currentSession()
                .createCriteria(Employee.class)
                .add(Restrictions.eq("yammerId", yammerId)), Employee.class);
        return employee.getUser();
    }

    @Override
    public void save(User user) {
        checkState(!user.isGuest(), "Cannot save a guest");
        super.save(user);
    }
}
