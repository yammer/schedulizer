package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.exceptions.EntityNotFoundException;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Random;

public class GroupManager extends EntityManager<Group> {

    private static final Random RANDOM = new Random();

    public GroupManager(SessionFactory sessionFactory) {
        super(sessionFactory, Group.class);
    }

    public Group random() {
        /* TODO: Hit db directly */
        List<Group> groups = all();
        if (groups.size() < 1) {
            throw new EntityNotFoundException(Group.class);
        }
        return groups.get(RANDOM.nextInt(groups.size()));
    }
}
