App.controller('GroupViewController', function($scope, $timeout, $location,  $stateParams,
                                              Group, GroupEmployee, AssignmentType, AssignableDay, EMPTY_GROUP) {

    var NEW_EMPLOYEE = {name: undefined, image: undefined}

    $scope.assignmentTypeBuckets = {};

    function getGroupEmployeesData(group) {
        if (group == EMPTY_GROUP) {
            group.employees = [];
            return;
        }
        group.employees = GroupEmployee.query({group_id: group.id}, function(employees) {
            group.employeeMap = _.indexBy(employees, 'id');
        });
    }

    $scope.addEmployee = function () {
        var group = $scope.selectedGroup;
        var yid = $scope.newEmployeeName; // TODO: retrieve from yammer
        if (yid == undefined || yid == "") { return; }

        var employee = new GroupEmployee({ groupId: group.id });
        employee.yammerId = yid;
        employee.$save({}, function(response) {
            group.employees.push(employee);
            $scope.newEmployeeName = "";
            group.employeeMap[employee.id] = employee;
        });

    }

    $scope.deleteEmployee = function (employee) {
        var group = $scope.selectedGroup;
        group.employees = _.without(group.employees, _.findWhere(group.employees, employee));
        employee.groupId = group.id;
        employee.$delete();
    }

    function getAssignmentTypeData(group) {
        if (group == EMPTY_GROUP) {
            group.assignmentTypes = [];
            return;
        }
        group.assignmentTypes = AssignmentType.query({ group_id: group.id });
    }

    $scope.addAssignmentType = function () {
        var name = $scope.newAssignmentTypeName;
        var group = $scope.selectedGroup;
        if (name == undefined || name == "") { return; }
        var assignmentType = new AssignmentType({ groupId: group.id });
        assignmentType.name = name;
        assignmentType.$save({}, function() {
            group.assignmentTypes.unshift(assignmentType);
            $scope.newAssignmentTypeName = "";
            initAssignmentTypeBucket(assignmentType);
        });

    }

    $scope.maybeAddAssignmentType = function() {
        if ($scope.isCreatingAssignmentType) {
            $scope.addAssignmentType();
        }
    }

    $scope.deleteAssignmentType = function (assignmentType) {
        var group = $scope.selectedGroup;
        assignmentType.groupId = group.id;
        assignmentType.$delete({}, function() {
            group.assignmentTypes.remove(assignmentType);
            delete $scope.assignmentTypeBuckets[assignmentType.id];
        });
    }

    function getCalendar() {
        // TODO: Find another way to do this
        return angular.element($("#group-tab .view-calendar-wrapper")[0]).scope();
    }

    $scope.goToToday = function() {
        // TODO: Find another way to do this
        var calendar = getCalendar();
        calendar.goToToday();
        $scope.dayStamp = new Date();
    }

    $scope.dayStamp = new Date(); /* Extract TODAY constant from calendar stuff */

    $scope.onHoverDay = function(day) {
        $scope.dayStamp = day.date;
    }

    $scope.clearSelection = function() {
        getCalendar().clearSelectedDays();
        $scope.selectedDays = [];
    }

    $scope.selectedDays = [];
    $scope.selectedDay = undefined;

    function initAssignmentTypeBucket(assignmentType) {
        $scope.assignmentTypeBuckets[assignmentType.id] = {
            employeeList: {},
            assignmentType: assignmentType
        };
    }

    function initAssignmentBuckets() {
        var assignmentTypes = $scope.selectedGroup.assignmentTypes;
        for (var i = 0; i < assignmentTypes.length; i++) {
            initAssignmentTypeBucket(assignmentTypes[i]);
        }
    }

    function updateAssignmentTypeBuckets(assignableDays) {
        for (var i = 0; i < assignableDays.length; i++) {
            for (var j = 0; j < assignableDays[i].assignments.length; j++) {
                var assignmentTypeId = assignableDays[i].assignments[j].assignmentTypeId;
                var employeeId = assignableDays[i].assignments[j].employeeId;
                var employee = $scope.selectedGroup.employeeMap[employeeId];
                if ($scope.assignmentTypeBuckets[assignmentTypeId].employeeList[employeeId] == undefined) {
                    $scope.assignmentTypeBuckets[assignmentTypeId].employeeList[employeeId] = {
                        employee: employee,
                        assignments: []
                    };
                }
                $scope
                    .assignmentTypeBuckets[assignmentTypeId]
                    .employeeList[employeeId]
                    .assignments.push(assignableDays[i].assignments[j].id);

                $scope.assignmentTypeBuckets[assignmentTypeId].employeeList[employeeId].assignments =
                    _.unique($scope.assignmentTypeBuckets[assignmentTypeId].employeeList[employeeId].assignments);
            }
        }
    }

    $scope.onSelectDays = function(days) {
        $scope.selectedDays = days;
        $scope.assignmentTypeBuckets = {};
        initAssignmentBuckets();
    }

    $scope.$watch('selectedGroup', function() {
        getGroupEmployeesData($scope.selectedGroup);
        getAssignmentTypeData($scope.selectedGroup);
    });

    var GroupViewDayContent = function(assignments) {
        this.assignments = assignments;
    }

    GroupViewDayContent.prototype.assignments = [];

    GroupViewDayContent.prototype.numberOfRoles = function() {
        return _.uniq(_.map(this.assignments, function(assignment) {
            return assignment.assignmentTypeId;
        })).length;
    }

    $scope.onLoadDayContent = function(days) {
        var startDate = days[0].date;
        var endDate = days[days.length - 1].date;
        days = _.indexBy(days, function(day) {
            return day.date.toISOLocalDateString();
        });

        console.log('trigger request from ' + startDate + ' to ' + endDate + ' of ' + days.length+ ' days');

        assignableDays = AssignableDay.query(
            {
                group_id: $scope.selectedGroup.id,
                start_date: startDate.toISOLocalDateString(),
                end_date: endDate.toISOLocalDateString()
            }, function(assignableDays) {
                console.log('returned');
                _.each(assignableDays, function(assignableDay) {
                    days[assignableDay.date].content = {
                        assignments: assignableDay.assignments
                    }
                });
            }
        );

    }

    var GroupViewDayContent = function(assignments) {
        this.assignments = assignments;
    }

    GroupViewDayContent.prototype.assignments = [];

    GroupViewDayContent.prototype.numberOfRoles = function() {
        return _.uniq(_.map(this.assignments, function(assignment) {
            return assignment.assignmentTypeId;
        })).length;
    }

    $scope.onLoadDayContent = function(days) {
        var startDate = days[0].date;
        var endDate = days[days.length - 1].date;
        days = _.indexBy(days, function(day) {
            return day.date.toISOLocalDateString();
        });

        console.log('trigger request from ' + startDate + ' to ' + endDate + ' of ' + days.length+ ' days');

        assignableDays = AssignableDay.query(
            {
                group_id: $scope.selectedGroup.id,
                start_date: startDate.toISOLocalDateString(),
                end_date: endDate.toISOLocalDateString()
            }, function(assignableDays) {
                console.log('returned');
                _.each(assignableDays, function(assignableDay) {
                    days[assignableDay.date].content = {
                        assignments: assignableDay.assignments
                    }
                });
            }
        );

    }

    $scope.addAssignment = function (employee, assignmentType) {
        var group = $scope.selectedGroup;
        var days = $scope.selectedDays;
        AssignableDay.save({
            groupId: group.id,
            employee_id:employee.id,
            assignment_type_id:assignmentType.id,
            dates: days.map(function(d) { return d.toISOLocalDateString() }).join()
        }, function(assignableDays) {
            updateAssignmentTypeBuckets(assignableDays);
        });
    }

    $scope.deleteAssignments = function(bucketEmployee, bucketEmployeeList) {
        var count = 0;
        var total = bucketEmployee.assignments.length;
        angular.forEach(bucketEmployee.assignments, function(assignmentId) {
            AssignableDay.delete({
                assignment_id: assignmentId,
                group_id: $scope.selectedGroup.id
            }, function() {
                count++;
                if (count == total) {
                    delete bucketEmployeeList[bucketEmployee.employee.id];
                }
            });
        });
    }


    // TODO: Ugly hack!
    $timeout(resizeCalendar, 300)
});
