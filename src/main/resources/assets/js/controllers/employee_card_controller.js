App.controller('EmployeeCardController', function ($scope, $rootScope) {
    $scope.isGroupAdmin = $rootScope.isGroupAdmin;
});

App.directive('employeeCard', function() {
    return {
        restrict: 'E',
        scope: {
            employee: "=",
            group: "=",
            remove: "&"
        },
        templateUrl: 'views/employee_card.html',
        controller: 'EmployeeCardController'
    };
});