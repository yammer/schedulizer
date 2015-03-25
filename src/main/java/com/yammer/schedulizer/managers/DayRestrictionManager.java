package com.yammer.schedulizer.managers;

import com.yammer.schedulizer.entities.DayRestriction;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.Group;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DayRestrictionManager extends EntityManager<DayRestriction> {

    public DayRestrictionManager(SessionFactory sessionFactory) {
        super(sessionFactory, DayRestriction.class);
    }

    @SuppressWarnings("unchecked")
    public List<DayRestriction> getByEmployeePeriod(Employee employee, LocalDate startDate, LocalDate endDate) {
        return currentSession()
                .createCriteria(DayRestriction.class)
                .add(Restrictions.eq("employee.id", employee.getId()))
                .add(Restrictions.ge("date", startDate))
                .add(Restrictions.le("date", endDate))
                .list();
    }

    public DayRestriction getOrCreateByEmployeeAndDate(Employee employee, LocalDate date) {
        DayRestriction dayRestriction = getUnique(currentSession()
                .createCriteria(DayRestriction.class)
                .add(Restrictions.eq("employee.id", employee.getId()))
                .add(Restrictions.eq("date", date)));
        if (dayRestriction == null) {
            dayRestriction = new DayRestriction(date, employee);
        }
        save(dayRestriction);
        return dayRestriction;
    }

    public List<DayRestriction> getByGroupPeriod(Group group, LocalDate startDate, LocalDate endDate) {
        Set<Employee> employees = group.getEmployees();
        List<DayRestriction> dayRestrictions = new ArrayList<>();
        for(Employee employee : employees) {
            dayRestrictions.addAll(getByEmployeePeriod(employee, startDate, endDate));
        }
        return dayRestrictions;
    }
}
