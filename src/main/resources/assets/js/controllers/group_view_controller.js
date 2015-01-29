App.controller('GroupViewController', function($scope, $timeout, $location,  $stateParams,
                                              Group, GroupEmployee, AssignmentType, AssignableDay, EMPTY_GROUP) {

    var NEW_EMPLOYEE = {name: undefined, image: undefined}

    var employeesMap = {};

    $scope.getGroupEmployeesData = function(group) {
        if (group == EMPTY_GROUP) {
            group.employees = [];
            return;
        }
        group.employees = GroupEmployee.query({ group_id: group.id }, function() {
            employeesMap = _.object(_.map(group.employees, function(item) {
                return [item.id, item]
            }));
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
            employeesMap[employee.id] = employee;
        });

    }

    $scope.deleteEmployee = function (employee) {
        var group = $scope.selectedGroup;
        group.employees = _.without(group.employees, _.findWhere(group.employees, employee));
        employee.groupId = group.id;
        employee.$delete();
    }

    $scope.getAssignmentTypeData = function(group) {
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
            console.log(assignmentType.id);
        });

    }

    $scope.deleteAssignmentType = function (assignmentType) {
        var group = $scope.selectedGroup;
        group.assignmentTypes = _.without(group.assignmentTypes, _.findWhere(group.assignmentTypes, assignmentType));
        assignmentType.groupId = group.id;
        assignmentType.$delete();
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

    function loadGroupData () {
        $scope.getGroupEmployeesData($scope.selectedGroup);
        $scope.getAssignmentTypeData($scope.selectedGroup);
    }

    $scope.clearSelection = function() {
        getCalendar().clearSelectedDays();
        $scope.selectedDays = [];
    }

    $scope.selectedDays = [];
    $scope.selectedDay = undefined;

    function updateAssignmentTypeBuckets(assignableDays) {
        for (var i = 0; i < assignableDays.length; i++) {
            for (var j = 0; j < assignableDays[i].assignments.length; j++) {
                var assignmentTypeId = assignableDays[i].assignments[j].assignmentTypeId;
                var employeeId = assignableDays[i].assignments[j].employeeId;
                var employee = employeesMap[employeeId];
                $scope.assignmentTypeBuckets[assignmentTypeId].employeeList.push(employee);
                $scope.assignmentTypeBuckets[assignmentTypeId].employeeList =
                    _.uniq($scope.assignmentTypeBuckets[assignmentTypeId].employeeList, function(e) {
                        return e.id;
                    });
            }
        }
    }

    $scope.onSelectDays = function(days) {
        $scope.selectedDays = days;
        $scope.assignmentTypeBuckets = {};
        var assignmentTypes = $scope.selectedGroup.assignmentTypes;
        for (var i = 0; i < assignmentTypes.length; i++) {
            $scope.assignmentTypeBuckets[assignmentTypes[i].id] = {
                employeeList: [], // TODO get this information from days
                assignmentType: assignmentTypes[i]
            };
        }
        console.log($scope.assignmentTypeBuckets);
    }

    $scope.$watch('selectedGroup', loadGroupData);

    function addAssignment(employee, assignmentType) {
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

    $scope.onDrop = function (dragEl, dropEl, employee, assignmentType) {
        addAssignment(employee, assignmentType);
    }

    // TODO: Ugly hack!
    $timeout(resizeCalendar, 300)
});
var k;
