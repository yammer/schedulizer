App.controller('EmployeeCardController', function ($scope) {

});

App.directive('employeeCard', function() {
    return {
        restrict: 'E',
        scope: {
            employee: "=",
            remove: "&"
        },
        templateUrl: 'views/employee_card.html',
        controller: 'EmployeeCardController'
    };
});