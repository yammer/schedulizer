package com.yammer.stresstime.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.stresstime.entities.AssignableDay;
import com.yammer.stresstime.entities.Assignment;

import java.util.*;
import java.util.stream.Collectors;

class Statistics {
    private long assignmentTypeId;
    private int count;

    public Statistics(long assignmentTypeId, int count) {
        this.assignmentTypeId = assignmentTypeId;
        this.count = count;
    }

    public long getAssignmentTypeId() {
        return assignmentTypeId;
    }

    public void setAssignmentTypeId(long assignmentTypeId) {
        this.assignmentTypeId = assignmentTypeId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void increment() {
        count++;
    }
}

class EmployeeStats {
    private long employeeId;
    private List<Statistics> statistics;

    public EmployeeStats(long employeeId, List<Statistics> statistics) {
        this.employeeId = employeeId;
        this.statistics = statistics;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    @JsonProperty("employee_id")
    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }

    public List<Statistics> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<Statistics> statistics) {
        this.statistics = statistics;
    }
}

class EmployeeStatsBuilder {
    private Map<Long, Statistics> statisticsMap;
    private long employeeId;

    public EmployeeStatsBuilder(long employeeId) {
        this.employeeId = employeeId;
        statisticsMap = new HashMap<>();
    }

    public void incrementAssignmentCount(Long assignmentTypeId) {
        Statistics stats = statisticsMap.get(assignmentTypeId);
        if (stats == null) {
            stats = new Statistics(assignmentTypeId, 0);
            statisticsMap.put(assignmentTypeId, stats);
        }
        stats.increment();
    }

    public EmployeeStats build() {
        return new EmployeeStats(employeeId, new ArrayList<>(statisticsMap.values()));
    }
}

public class AssignmentsStatsBuilder {

    private Map<Long, EmployeeStatsBuilder> employeeStatsMap;

    public AssignmentsStatsBuilder(List<AssignableDay> assignableDays) {
        employeeStatsMap = new HashMap<>();
        for (AssignableDay assignableDay : assignableDays) {
            Set<Assignment> assignments = assignableDay.getAssignments();
            for (Assignment assignment : assignments) {
                long employeeId = assignment.getEmployeeId();
                EmployeeStatsBuilder employeeStatsBuilder = employeeStatsMap.get(employeeId);
                if (employeeStatsBuilder == null) {
                    employeeStatsBuilder = new EmployeeStatsBuilder(employeeId);
                    employeeStatsMap.put(employeeId, employeeStatsBuilder);
                }
                employeeStatsBuilder.incrementAssignmentCount(assignment.getAssignmentTypeId());
            }

        }
    }

    public List<EmployeeStats> build() {
        return employeeStatsMap.values().stream()
                .map(e -> e.build())
                .collect(Collectors.toList());
    }
}
