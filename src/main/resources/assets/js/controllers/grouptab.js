App.controller('GroupTabController', function ($scope) {
    $scope.groupList = [{name: "group1"}, {name: "group2"}];
});

App.directive('grouptab', function() {
    return {
        restrict: 'E',
        templateUrl: 'html/partials/grouptab.html',
        controller: 'GroupTabController'
    };
});