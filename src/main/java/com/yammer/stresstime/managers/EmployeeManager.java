package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Employee;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class EmployeeManager extends EntityManager<Employee> {

    public EmployeeManager(SessionFactory sessionFactory) {
        super(sessionFactory, Employee.class);
    }

    public List<Employee> all() {
        return list(currentSession().createQuery("from Employee"));
    }

    public Employee getByYammerId(String yammerId) {
        return (Employee) currentSession()
                .createCriteria(Employee.class)
                .add(Restrictions.eq("mYammerId", yammerId))
                .uniqueResult();
    }

    public Employee getOrCreateByYammerId(String yammerId) {
        Employee employee = getByYammerId(yammerId);
        if (employee == null) {
            employee = createByYammerId(yammerId);
        }
        return employee;
    }

    private Employee createByYammerId(String yammerId) {
        /* TODO */
        Employee employee = new Employee("name retrieved from yammer", yammerId);
        employee.setYammerId(yammerId);
        employee.setGlobalAdmin(false);
        // TODO: set name, image and other information
        save(employee);
        if (employee.getId() == 0) {
            /* TODO: Remove this */
            throw new AssertionError();
        }
        return employee;
    }
}
