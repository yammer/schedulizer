package com.yammer.stresstime.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "memberships",
        uniqueConstraints = @UniqueConstraint(columnNames =
                {"employee_id", "group_id"}))
public class Membership extends JsonAnnotatedEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "admin")
    @NotNull
    private boolean admin = false;

    /* package private */ Membership() {
        // Required by Hibernate
    }

    public Membership(Employee employee, Group group) {
        this.employee = employee;
        this.group = group;
    }

    public long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
