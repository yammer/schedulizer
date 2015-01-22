App.controller('GroupTabController', function($scope, $timeout, $location, $routeParams,
                                              Group, GroupEmployee, AssignmentType) {
    console.log($routeParams);

    var EMPTY_GROUP = {id: undefined, name: "-"}
    var NEW_EMPLOYEE = {name: undefined, image: undefined}

    $scope.selectedGroup = EMPTY_GROUP;

     function initGroupsData (callback) {
        console.log("getting group");
        $scope.groups = Group.query({}, function (groups, responseHeaders) {
            if (groups.length > 0) {
                if ($routeParams.groupId == 'default') {
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

    $scope.createNewGroup = function(groupName) {
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
        });
    }

    $scope.deleteGroup = function(group) {
        $scope.groups = _.without($scope.groups, _.findWhere($scope.groups, group));
        group.$delete();
    }

    $scope.getGroupEmployeesData = function(group) {
        group.employees = GroupEmployee.query({ group_id: group.id });
    }

    $scope.addEmployee = function (yid, group) {
        if (yid == undefined || yid == "") { return; }
        var employee = new GroupEmployee({ groupId: group.id });
        employee.yid = yid;
        employee.$save();
        group.employees.push(employee);
    }

    $scope.deleteEmployee = function (group, employee) {
        group.employees = _.without(group.employees, _.findWhere(group.employees, employee));
        employee.groupId = group.id;
        employee.$delete();
    }

    $scope.getAssignmentTypeData = function(group) {
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

    // TODO: Extract concerns (has to link groups and employees either way)
    $scope.employees = [];

    $scope.newEmployee = new Employee();

    $scope.addEmployee = function() {
        // TODO: Use yammer auto-complete, hit backend for saving
        var employee = $scope.newEmployee;
        $scope.newEmployee = new Employee();
        employee.image = "https://mug0.assets-yammer.com/mugshot/images/75x75/bsrr0LTDpcX3pZvt59FZtn1KTRp5J9Fm";
        $scope.employees.push(employee);
        //employee.$save();
    }

    $scope.removeEmployee = function(employee) {
        //employee.$delete();
        var i = $scope.employees.indexOf(employee);
        $scope.employees.splice(i, 1);
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

    initGroupsData(function() {
        $scope.getGroupEmployeesData($scope.selectedGroup);
        $scope.getAssignmentTypeData($scope.selectedGroup);
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
