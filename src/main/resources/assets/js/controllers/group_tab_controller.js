App.controller('GroupTabController', function($scope, $timeout, $location, $routeParams, Group, Employee) {

    var EMPTY_GROUP = {id: undefined, name: "-"}
    var NEW_EMPLOYEE = {name: undefined, image: undefined}

    $scope.selectedGroup = EMPTY_GROUP;

    $scope.groups = Group.query({}, function(groups, responseHeaders) {
        if (groups.length > 0) {
            if ($routeParams.groupId == 'default') {
                // redirects
                $location.path('group/' + groups[0].id)
            }
            $scope.selectedGroup = _.find(groups, function(g) {
                return g.id == parseInt($routeParams.groupId);
            }) || EMPTY_GROUP;
            if ($scope.selectedGroup != EMPTY_GROUP) {
                $scope.employees = Employee.query({
                    groupId: $scope.selectedGroup.id
                })
            }
        }
    });

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

    $scope.clearSelection = function() {
        getCalendar().clearSelectedDays();
        $scope.selectedDays = [];
    }

    $scope.selectedDays = [];

    $scope.onSelectDays = function(days) {
        $scope.selectedDays = days;
    }

    // TODO: Ugly hack!
    $timeout(resizeCalendar, 300)
});
