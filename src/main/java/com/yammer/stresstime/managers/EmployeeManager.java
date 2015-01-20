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

    public Employee createNewEmployee(String yammer_id) {
        Employee employee = new Employee();
        employee.setYammerId(yammer_id);
        employee.setGlobalAdmin(false);
        employee.setName("name retrieved from yammer");
        // TODO: set name, image and other information
        employee = save(employee);
        return employee;
    }
}
