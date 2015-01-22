App.controller('GroupTabController', function($scope, $timeout, $location, $routeParams, Group) {
    console.log($routeParams);

    var EMPTY_GROUP = {id: undefined, name: "-"}

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
        }
    });

    $scope.isSelectedGroup = function(group) {
        return group && group.id == $scope.selectedGroup.id;
    }

    $scope.goToToday = function() {
        // TODO: Find another way to do this
        var calendar = angular.element($("#group-tab .calendar")[0]).scope();
        calendar.goToToday();
    }

    $scope.selectedDay = undefined;

    // TODO: expose method in directive not this way
    $scope.selectDay = function(day) {
        console.log("selectDay")
        $scope.selectedDay = day;
    }

    // TODO: Ugly hack!
    $timeout(resizeCalendar, 300)
});
