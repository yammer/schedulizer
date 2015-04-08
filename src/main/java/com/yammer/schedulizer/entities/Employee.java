package com.yammer.schedulizer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees",
        uniqueConstraints = @UniqueConstraint(columnNames =
                {"ext_app_id"}))
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "yammer_id")
    private String yammerId;

    @Column(name = "ext_app_id")
    @NotEmpty
    private String extAppId;

    @Column(name = "name")
    @NotBlank
    private String name;

    @Column(name = "global_admin")
    @NotNull
    private boolean globalAdmin = false;

    @Column(name = "image_url_template")
    private String imageUrlTemplate;

    @OneToMany(mappedBy = "employee")
    @Cascade({CascadeType.DELETE})
    private Set<Membership> memberships = new HashSet<>();

    @OneToMany(mappedBy = "employee")
    @Cascade({CascadeType.DELETE})
    private Set<DayRestriction> dayRestrictions = new HashSet<>();

    @OneToMany(mappedBy = "employee")
    private Set<Assignment> assignments = new HashSet<>();

    @OneToOne(mappedBy = "employee", fetch = FetchType.EAGER)
    @JsonIgnore
    private User user;

    /* package private */ Employee() {
        // Required by Hibernate
    }

    public Employee(String name, String extAppId) {
        this.name = name;
        this.extAppId = extAppId;
        yammerId = extAppId;
    }

    public Employee(String extAppId) {
        this.extAppId = extAppId;
        yammerId = extAppId;
    }

    public long getId() {
        return id;
    }

    public String getExtAppId() { return extAppId; }

    public void setExtAppId(String extAppId) { this.extAppId = extAppId; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGlobalAdmin() {
        return globalAdmin;
    }

    public void setGlobalAdmin(boolean globalAdmin) {
        this.globalAdmin = globalAdmin;
    }

    public String getImageUrlTemplate() {
        return imageUrlTemplate;
    }

    public void setImageUrlTemplate(String imageUrlTemplate) {
        this.imageUrlTemplate = imageUrlTemplate;
    }

    @JsonIgnore
    public Set<Membership> getMemberships() {
        return ImmutableSet.copyOf(memberships);
    }

    @JsonIgnore
    public Set<Group> getGroups() {
        ImmutableSet.Builder<Group> groups = new ImmutableSet.Builder<>();
        for (Membership membership : memberships) {
            groups.add(membership.getGroup());
        }
        return groups.build();
    }

    @JsonIgnore
    public Set<Assignment> getAssignments() {
        return ImmutableSet.copyOf(assignments);
    }

    public void setAssignments(Set<Assignment> assignments) {
        this.assignments = ImmutableSet.copyOf(assignments);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonIgnore
    public Set<DayRestriction> getDayRestrictions() {
        return ImmutableSet.copyOf(dayRestrictions);
    }

    public void setDayRestrictions(Set<DayRestriction> dayRestrictions) {
        this.dayRestrictions = ImmutableSet.copyOf(dayRestrictions);
    }
}
