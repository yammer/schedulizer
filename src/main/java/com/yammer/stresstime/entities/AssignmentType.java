package com.yammer.stresstime.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;

@Entity
@Table(name = "assignment_types")
public class AssignmentType {

    public AssignmentType() {
        // Required by Hibernate
    }

    public AssignmentType(String name, Group group) {
        setName(name);
        setGroup(group);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mId;

    @Column(name = "name")
    @NotBlank
    private String mName;

    @Column(name = "description")
    private String mDescription;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group mGroup;

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    @JsonIgnore
    public Group getGroup() {
        return mGroup;
    }

    public void setGroup(Group group) {
        mGroup = group;
    }
}
