App.controller('GroupTabController', function ($scope, $timeout) {
    $scope.groupList = [{name: "group1"}, {name: "group2"}];
    $scope.$watch('selectedGroup.name', function() {

    });

    // TODO: Ugly hack!
    $timeout(resizeCalendar, 300)
});
