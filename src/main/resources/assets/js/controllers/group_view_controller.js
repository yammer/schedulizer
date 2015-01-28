App.controller('GroupViewController', function($scope, $timeout, $location,  $stateParams,
                                              Group, GroupEmployee, AssignmentType, EMPTY_GROUP) {

    var NEW_EMPLOYEE = {name: undefined, image: undefined}

    $scope.getGroupEmployeesData = function(group) {
        if (group == EMPTY_GROUP) {
            group.employees = [];
            return;
        }
        group.employees = GroupEmployee.query({ group_id: group.id });
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
        name = $scope.newAssignmentName;
        group = $scope.selectedGroup;
        if (name == undefined || name == "") { return; }
        var assignmentType = new AssignmentType({ groupId: group.id });
        assignmentType.name = name;
        assignmentType.$save({}, function() {
            group.assignmentTypes.push(assignmentType);
            $scope.newAssignmentName = "";
        });

    }

    $scope.deleteAssignmentType = function (group, assignmentType) {
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

    $scope.onSelectDays = function(days) {
        $scope.selectedDays = days;
    }

    $scope.$watch('selectedGroup', loadGroupData);

    $scope.onDrop = function (dragEl, dropEl) {

    }

    // TODO: Ugly hack!
    $timeout(resizeCalendar, 300)
});
var k;
