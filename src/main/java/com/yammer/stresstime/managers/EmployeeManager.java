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
        return getUnique(currentSession()
                .createCriteria(Employee.class)
                .add(Restrictions.eq("yammerId", yammerId)));
    }

    public Employee getOrCreateByYammerId(String yammerId, String name, String img) {
        Employee employee = getByYammerId(yammerId);
        if (employee == null) {
            employee = createByYammerId(yammerId, name, img);
        }
        return employee;
    }

    private Employee createByYammerId(String yammerId, String name, String img) {
        /* TODO */
        Employee employee = new Employee(name, yammerId);
        employee.setImageUrlTemplate(img);
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
