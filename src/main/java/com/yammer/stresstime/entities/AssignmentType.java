package com.yammer.stresstime.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

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
    private long id;

    @Column(name = "name")
    @NotBlank
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToMany(mappedBy = "assignmentType")
    @Cascade({CascadeType.DELETE})
    private Set<Assignment> assignments = new HashSet<>();

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
