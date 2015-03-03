package com.yammer.stresstime.managers;

import com.google.common.collect.Maps;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.utils.CoreUtils;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssignableDayManager extends EntityManager<AssignableDay> {

    public AssignableDayManager(SessionFactory sessionFactory) {
        super(sessionFactory, AssignableDay.class);
    }

    public AssignableDay getOrCreateByGroupAndDate(Group group, LocalDate date) {
        AssignableDay assignableDay = getUnique(currentSession()
                .createCriteria(AssignableDay.class)
                .add(Restrictions.eq("date", date))
                .add(Restrictions.eq("group", group)));
        if (assignableDay == null) {
            assignableDay = new AssignableDay(group, date);
            save(assignableDay);
        }
        return assignableDay;
    }

    @SuppressWarnings("unchecked")
    public List<AssignableDay> getByGroupPeriod(Group group, LocalDate startDate, LocalDate endDate) {
        return currentSession()
                .createCriteria(AssignableDay.class)
                .add(Restrictions.eq("group.id", group.getId()))
                .add(Restrictions.ge("date", startDate))
                .add(Restrictions.le("date", endDate))
                .list();
    }
}
