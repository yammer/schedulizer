package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Group;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Random;

public class GroupManager extends EntityManager<Group> {

    private static final Random RANDOM = new Random();

    public GroupManager(SessionFactory sessionFactory) {
        super(sessionFactory, Group.class);
    }

    public List<Group> all() {
        return list(currentSession().createQuery("from Group"));
    }

    public Group random() {
        /* TODO: Hit db directly */
        List<Group> groups = all();
        return groups.get(RANDOM.nextInt(groups.size()));
    }

}
