package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Employee;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

public class EmployeeManager extends EntityManager<Employee> {

    public EmployeeManager(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Employee findByYammerId(String yammerId) {
        return (Employee) currentSession()
                .createCriteria(Employee.class)
                .add(Restrictions.eq("mYammerId", yammerId))
                .uniqueResult();
    }
}
