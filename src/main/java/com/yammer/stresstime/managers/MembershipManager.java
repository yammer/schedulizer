package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;

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

    public List<Membership> getGroupAdmins(long groupId) {
        return currentSession()
                .createCriteria(Membership.class)
                .add(Restrictions.eq("group.id", groupId))
                .add(Restrictions.eq("admin", true)).list();
    }

}
