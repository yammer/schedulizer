package com.yammer.schedulizer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.schedulizer.auth.Authentication;
import com.yammer.schedulizer.auth.Role;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    public static User fresh(Employee employee, String accessToken) {
        User user = new User(employee, accessToken);
        user.renew();
        return user;
    }

    public static User guest() {
        return new User();
    }

    /* TODO: Sure? */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "ext_app_type")
    private String extAppType;

    @Column(name = "expiration_date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    private LocalDate expirationDate;

    /* package private */ User() {
        // Required by Hibernate
    }

    public User(Employee employee, String accessToken) {
        this.employee = employee;
        this.accessToken = accessToken;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getExtAppType() { return extAppType; }

    public void setExtAppType(String extAppType) { this.extAppType = extAppType; }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        /* TODO: Expire? */
    }

    @JsonIgnore
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    @JsonProperty("expirationDate")
    public String getExpirationDateString() {
        return expirationDate.toString();
    }

    public Role getRole() {
        if (employee == null) return Role.GUEST;
        if (employee.isGlobalAdmin()) return Role.ADMIN;
        return Role.MEMBER;
    }

    public boolean isGuest() {
        return getRole() == Role.GUEST;
    }

    public boolean isUpToDate() {
        return expirationDate != null && expirationDate.isAfter(LocalDate.now());
    }

    public void renew() {
        expirationDate = LocalDate.now().plusDays(Authentication.EXPIRATION_TIME);
    }

    public void expire() {
        expirationDate = null;
    }

    @Override
    public long getId() {
        return id;
    }
}
