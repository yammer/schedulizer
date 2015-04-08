package com.yammer.schedulizer.fixtures;

import com.google.common.collect.Lists;
import com.yammer.schedulizer.auth.ExtAppType;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.test.TestUtils;
import org.hibernate.SessionFactory;

import java.util.List;

public class EmployeesFixture {

    private List<Employee> employees;
    private boolean saved;

    public EmployeesFixture() {
        saved = false;
        employees = Lists.newArrayList(new Employee("John", TestUtils.nextExtAppId(), ExtAppType.yammer),
                new Employee("Mary", TestUtils.nextExtAppId(), ExtAppType.yammer),
                new Employee("Louise", TestUtils.nextExtAppId(), ExtAppType.yammer));
    }

    public void save(SessionFactory sessionFactory) {
        if (saved) return;
        saved = true;
        EmployeeManager employeeManager = new EmployeeManager(sessionFactory);
        employees.stream().forEach(e -> employeeManager.save(e));
    }

    public List<Employee> getEmployees() {
        return employees;
    }
}
