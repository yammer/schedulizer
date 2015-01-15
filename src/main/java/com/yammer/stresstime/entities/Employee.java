package com.yammer.stresstime.entities;

import com.google.common.collect.ImmutableSet;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mId;

    @Column(name = "yammer_id")
    @NotEmpty
    private String mYammerId;

    @Column(name = "name")
    @NotBlank
    private String mName;

    @Column(name = "global_admin")
    @NotNull
    private boolean mGlobalAdmin = false;

    @Column(name = "image_url_template")
    private String mImageUrlTemplate;

    @OneToMany(mappedBy = "mEmployee")
    private Set<Membership> mMemberships = new HashSet<>();

    @OneToMany(mappedBy = "mEmployee")
    private Set<Assignment> mAssignments = new HashSet<>();

    public long getId() {
        return mId;
    }

    public String getYammerId() {
        return mYammerId;
    }

    public void setYammerId(String yammerId) {
        mYammerId = yammerId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isGlobalAdmin() {
        return mGlobalAdmin;
    }

    public void setGlobalAdmin(boolean globalAdmin) {
        mGlobalAdmin = globalAdmin;
    }

    public String getImageUrlTemplate() {
        return mImageUrlTemplate;
    }

    public void setImageUrlTemplate(String imageUrlTemplate) {
        mImageUrlTemplate = imageUrlTemplate;
    }

    public Set<Membership> getMemberships() {
        return ImmutableSet.copyOf(mMemberships);
    }

    public Set<Group> getGroups() {
        ImmutableSet.Builder<Group> groups = new ImmutableSet.Builder<>();
        for (Membership membership : mMemberships) {
            groups.add(membership.getGroup());
        }
        return groups.build();
    }

    public Set<Assignment> getAssignments() {
        return ImmutableSet.copyOf(mAssignments);
    }

    public void setAssignments(Set<Assignment> assignments) {
        mAssignments = ImmutableSet.copyOf(assignments);
    }
}
