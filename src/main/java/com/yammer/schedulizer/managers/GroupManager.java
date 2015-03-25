package com.yammer.schedulizer.managers;

import com.yammer.schedulizer.entities.Group;
import org.hibernate.SessionFactory;

public class GroupManager extends EntityManager<Group> {

    public GroupManager(SessionFactory sessionFactory) {
        super(sessionFactory, Group.class);
    }
}
