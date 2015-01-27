App.controller('GroupTabController', function($scope, $timeout, $location, $routeParams,
                                              Group, GroupEmployee, AssignmentType) {
    console.log($routeParams);

    var EMPTY_GROUP = {id: undefined, name: "-"}
    var NEW_EMPLOYEE = {name: undefined, image: undefined}

    $scope.selectedGroup = EMPTY_GROUP;

     function initGroupsData (callback) {
        $scope.groups = Group.query({}, function (groups, responseHeaders) {
            if (groups.length > 0) {
                var foundGroup = _.any($scope.groups, function(g) {
                    return g.id == $routeParams.groupId
                });
                if (!foundGroup) {
                    // redirects
                    $location.path('group/' + groups[0].id)
                }
            }
            $scope.selectedGroup = _.find(groups, function(g) {
                return g.id == parseInt($routeParams.groupId);
            }) || EMPTY_GROUP;
            callback();
        });
    }

    $scope.createNewGroup = function() {
        var groupName = $scope.newGroupName;
        if(groupName == undefined || groupName == "") { return; }

        var group = new Group();
        group.name = groupName;
        group.$save(function() {
            if ($scope.groups.length == 0) {
                $timeout( function(){
                    $scope.groups = Group.query({});
                },0);
            }
            else {
                $scope.groups.push(group);
            }
            $scope.newGroupName = "";
        });
    }

    $scope.deleteGroup = function(group) {
        group.$delete({}, function() {
            $scope.groups = _.without($scope.groups, _.findWhere($scope.groups, group));
            if ($scope.isSelectedGroup(group)) {
                if ($scope.groups.length == 0) {
                    $scope.selectGroup(EMPTY_GROUP);
                }
                else {
                    $scope.selectGroup($scope.groups[0]);
                }
            }
        });

    }

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
        employee.yid = yid;
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

    $scope.addAssignmentType = function (name, group) {
        if (name == undefined || name == "") { return; }
        var assignmentType = new AssignmentType({ groupId: group.id });
        assignmentType.name = name;
        assignmentType.$save();
        group.assignmentTypes.push(assignmentType);
    }

    $scope.deleteAssignmentType = function (group, assignmentType) {
        group.assignmentTypes = _.without(group.assignmentTypes, _.findWhere(group.assignmentTypes, assignmentType));
        assignmentType.groupId = group.id;
        assignmentType.$delete();
    }

    $scope.isSelectedGroup = function(group) {
        return group && group.id == $scope.selectedGroup.id;
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

    $scope.selectGroup = function (group) {
        $scope.selectedGroup = group;
        $scope.getGroupEmployeesData($scope.selectedGroup);
        $scope.getAssignmentTypeData($scope.selectedGroup);
    }

    initGroupsData(function() {
        $scope.selectGroup($scope.selectedGroup);
    });

    $scope.clearSelection = function() {
        getCalendar().clearSelectedDays();
        $scope.selectedDays = [];
    }

    $scope.selectedDays = [];
    $scope.selectedDay = undefined;

    $scope.onSelectDays = function(days) {
        $scope.selectedDays = days;
    }

    // TODO: Ugly hack!
    $timeout(resizeCalendar, 300)
});
