package com.yammer.stresstime.fixtures;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.test.TestUtils;
import org.hibernate.SessionFactory;

import java.util.List;

public class EmployeesFixture {

    private List<Employee> employees;
    private boolean saved;

    public EmployeesFixture() {
        saved = false;
        employees = Lists.newArrayList(new Employee("John", TestUtils.nextYammerId()),
                new Employee("Mary", TestUtils.nextYammerId()),
                new Employee("Louise", TestUtils.nextYammerId()));
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
