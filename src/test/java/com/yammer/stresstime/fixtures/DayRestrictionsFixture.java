package com.yammer.stresstime.fixtures;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.DayRestriction;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.managers.DayRestrictionManager;
import org.hibernate.SessionFactory;
import org.joda.time.LocalDate;

import java.util.List;

public class DayRestrictionsFixture {

    private List<DayRestriction> dayRestrictions;
    private EmployeesFixture employeesFixture;
    private boolean saved;

    public DayRestrictionsFixture(EmployeesFixture employeesFixture) {
        this.employeesFixture = employeesFixture;
        saved = false;
        List<Employee> employees = employeesFixture.getEmployees();
        dayRestrictions = Lists.newArrayList(new DayRestriction(new LocalDate(2015,2,9), employees.get(0)),
                new DayRestriction(new LocalDate(2015,2,10), employees.get(0)),
                new DayRestriction(new LocalDate(2015,2,11), employees.get(0)),
                new DayRestriction(new LocalDate(2015,2,15), employees.get(0)),
                new DayRestriction(new LocalDate(2015,3,10), employees.get(0)),
                new DayRestriction(new LocalDate(2015,3,12), employees.get(0)),
                new DayRestriction(new LocalDate(2015,3,15), employees.get(0)),
                new DayRestriction(new LocalDate(2015,5,10), employees.get(0)),
                new DayRestriction(new LocalDate(2015,2,11), employees.get(1)),
                new DayRestriction(new LocalDate(2015,2,12), employees.get(1)),
                new DayRestriction(new LocalDate(2015,2,13), employees.get(1)),
                new DayRestriction(new LocalDate(2015,2,15), employees.get(1)),
                new DayRestriction(new LocalDate(2015,2,20), employees.get(1)),
                new DayRestriction(new LocalDate(2015,2,10), employees.get(2)));
    }

    public void save(SessionFactory sessionFactory) {
        if (saved) return;
        saved = true;
        DayRestrictionManager dayRestrictionManager = new DayRestrictionManager(sessionFactory);
        dayRestrictions.stream().forEach(e -> dayRestrictionManager.save(e));
    }

    public List<DayRestriction> getDayRestrictions() {
        return dayRestrictions;
    }
}
