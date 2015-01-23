package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Membership;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

public class MembershipManager extends EntityManager<Membership> {

    public MembershipManager(SessionFactory sessionFactory) {
        super(sessionFactory, Membership.class);
    }

    public Membership getByEmployeeAndGroup(Long employeeId, Long groupId) {
        Membership membership = (Membership) currentSession()
                .createCriteria(Membership.class)
                .add(Restrictions.eq("mEmployee.mId", employeeId))
                .add(Restrictions.eq("mGroup.mId", groupId))
                .uniqueResult();
        return membership;
    }

    public boolean deleteByEmployeeAndGroup(Long employeeId, Long groupId) {
        Membership membership = getByEmployeeAndGroup(employeeId, groupId);
        if (membership == null) return false;
        try {
            currentSession().delete(membership);
            return true;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

}
