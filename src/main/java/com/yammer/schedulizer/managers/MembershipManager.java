package com.yammer.schedulizer.managers;

import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.Group;
import com.yammer.schedulizer.entities.Membership;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

public class MembershipManager extends EntityManager<Membership> {

    public MembershipManager(SessionFactory sessionFactory) {
        super(sessionFactory, Membership.class);
    }

    public Membership join(Group group, Employee employee) {
        // TODO: Validate uniqueness of (group, employee) in the group
        Membership membership = new Membership(employee, group);
        save(membership);
        return membership;
    }

    public Membership getByEmployeeIdAndGroupId(long employeeId, long groupId) {
        return getExactOne(currentSession()
                .createCriteria(Membership.class)
                .add(Restrictions.eq("employee.id", employeeId))
                .add(Restrictions.eq("group.id", groupId)));
    }
}
