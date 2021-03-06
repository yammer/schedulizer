package com.yammer.schedulizer.managers;

import com.yammer.schedulizer.auth.ExtAppType;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.User;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.function.Consumer;

public class EmployeeManager extends EntityManager<Employee> {

    public EmployeeManager(SessionFactory sessionFactory) {
        super(sessionFactory, Employee.class);
    }

    public Employee safeGetByExtAppId(String extAppId, ExtAppType extAppType) {
        return getUnique(currentSession()
                .createCriteria(Employee.class)
                .add(Restrictions.eq("extAppId", extAppId))
                .add(Restrictions.eq("extAppType", extAppType.toString())));
    }

    public Employee getByExtAppId(String extAppId, ExtAppType extAppType) {
        Employee employee = safeGetByExtAppId(extAppId, extAppType);
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
     * Try to find an employee with the provided extAppId, if not found create one with the extAppId,
     * in which case the callback is called before saving.
     *
     * @param extAppId The external app id to look for or to initialize the new employee with.
     * @param callback It's called if no employee is found with the new employee as parameter before saving.
     * @return The found or newly created employee
     */
    public Employee getOrCreateByExtAppId(String extAppId, ExtAppType extAppType, Consumer<Employee> callback) {
        Employee employee = safeGetByExtAppId(extAppId, extAppType);
        if (employee == null) {
            employee = new Employee(extAppId, extAppType);
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

    public long count(ExtAppType extAppType) {
        return ((Number) currentSession()
                .createCriteria(Employee.class)
                .add(Restrictions.eq("extAppType", extAppType.toString()))
                .setProjection(Projections.rowCount())
                .uniqueResult())
                .longValue();
    }
}
