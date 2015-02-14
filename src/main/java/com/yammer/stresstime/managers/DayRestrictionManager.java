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

    public List<DayRestriction> getByEmployeePeriod(Employee employee, LocalDate startDate, LocalDate endDate) {
        return currentSession()
                .createCriteria(DayRestriction.class)
                .add(Restrictions.eq("employee.id", employee.getId()))
                .add(Restrictions.ge("date", startDate))
                .add(Restrictions.le("date", endDate))
                .list();
    }

    public DayRestriction getOrCreateByEmployeeDateComment(Employee employee, LocalDate date, String comment, int restrictionLevel) {
        DayRestriction dayRestriction = getUnique(currentSession()
                .createCriteria(DayRestriction.class)
                .add(Restrictions.eq("employee.id", employee.getId()))
                .add(Restrictions.eq("date", date)));
        if (dayRestriction == null) {
            dayRestriction = new DayRestriction(date, employee);
        }
        dayRestriction.setComment(comment);
        dayRestriction.setRestrictionLevel(restrictionLevel);
        save(dayRestriction);
        return dayRestriction;
    }
}
