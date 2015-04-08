package com.yammer.schedulizer.managers;

import com.yammer.schedulizer.entities.Employee;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.function.Consumer;

public class EmployeeManager extends EntityManager<Employee> {

    public EmployeeManager(SessionFactory sessionFactory) {
        super(sessionFactory, Employee.class);
    }

    public Employee safeGetByYammerId(String yammerId) {
        return getUnique(currentSession()
                .createCriteria(Employee.class)
                .add(Restrictions.eq("extAppId", yammerId)));
    }

    public Employee getByYammerId(String yammerId) {
        Employee employee = safeGetByYammerId(yammerId);
        return checkFound(employee);
    }

    @SuppressWarnings("unchecked")
    public List<Employee> getGlobalAdmins() {
        return currentSession()
                .createCriteria(Employee.class)
                .add(Restrictions.eq("globalAdmin", true))
                .list();
    }

    /**
     * Try to find an employee with the provided yammerId, if not found create one with the yammerId,
     * in which case the callback is called before saving.
     *
     * @param yammerId The yammer id to look for or to initialize the new employee with.
     * @param callback It's called if no employee is found with the new employee as parameter before saving.
     * @return The found or newly created employee
     */
    public Employee getOrCreateByYammerId(String yammerId, Consumer<Employee> callback) {
        Employee employee = safeGetByYammerId(yammerId);
        if (employee == null) {
            employee = new Employee(yammerId);
            callback.accept(employee);
            save(employee);
        }
        return employee;
    }

    public void updateByEmployeeId(long id, Consumer<Employee> callback) {
        Employee employee = getById(id);
        if (employee != null) {
            callback.accept(employee);
            save(employee);
        }
    }
}
