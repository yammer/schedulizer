package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.DayRestriction;
import com.yammer.stresstime.entities.Employee;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

import java.util.List;

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
}
