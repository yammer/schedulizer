package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.AssignableDay;
import com.yammer.stresstime.entities.Group;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

import java.util.List;

public class AssignableDayManager extends EntityManager<AssignableDay> {

    public AssignableDayManager(SessionFactory sessionFactory) {
        super(sessionFactory, AssignableDay.class);
    }

    public AssignableDay getByGroupDate(Group group, LocalDate date) {
        return (AssignableDay) currentSession()
                .createCriteria(AssignableDay.class)
                .add(Restrictions.eq("mDate", date))
                .add(Restrictions.eq("mGroup", group))
                .uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<AssignableDay> getByGroupPeriod(Group group, LocalDate startDate, LocalDate endDate) {
        return currentSession()
                .createCriteria(AssignableDay.class)
                .add(Restrictions.ge("mDate", startDate))
                .add(Restrictions.le("mDate", endDate)).list();
    }
}
