package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.managers.exceptions.EntityNonUniqueException;
import com.yammer.stresstime.managers.exceptions.EntityNotFoundException;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

public class MembershipManager extends EntityManager<Membership> {

    public MembershipManager(SessionFactory sessionFactory) {
        super(sessionFactory, Membership.class);
    }

    public Membership join(Group group, Employee employee) {
        // Validate uniqueness of (group, employee) in the group
        Membership membership = new Membership(employee, group);
        save(membership);
        return membership;
    }

    public Membership getByEmployeeIdAndGroupId(long employeeId, long groupId) {
        Criteria criteria = currentSession()
                .createCriteria(Membership.class)
                .add(Restrictions.eq("mEmployee.mId", employeeId))
                .add(Restrictions.eq("mGroup.mId", groupId));
        Membership membership;
        try {
            membership = (Membership) criteria.uniqueResult();
        } catch (HibernateException e) {
            EntityNonUniqueException error = new EntityNonUniqueException(Membership.class);
            error.initCause(e);
            throw error;
        }
        if (membership == null) {
            throw new EntityNotFoundException(Membership.class);
        }
        return membership;
    }

}
