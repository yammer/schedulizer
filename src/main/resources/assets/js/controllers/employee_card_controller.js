App.controller('EmployeeCardController', function($scope, $rootScope) {
    $scope.isGroupAdmin = $rootScope.isGroupAdmin;
    $scope.isGroupMember = $rootScope.isGroupMember;
    $scope.isGroupMemberOrGlobalAdmin = $rootScope.isGroupMemberOrGlobalAdmin;
    $scope.isGlobalAdmin = $rootScope.isGlobalAdmin;

    $scope.toggleAdminClicked = function(employee) {
        if (!$scope.isGroupAdmin($scope.group)) {
            return;
        }
        if(employee.globalAdmin) { return; }
        $scope.toggleAdmin(employee);
    };

    $scope.employeeNameContainer = null; // <div.name>
    $scope.employeeName = null; // <div.name span>
    $scope.sampleBadge = null; // <.mini-assignment-type-label> can overwrite, just need a sample
});

App.directive('employeeCard', function() {
    return {
        restrict: 'E',
        scope: {
            employee: "=",
            group: "=",
            remove: "&",
            toggleAdmin: "="
        },
        templateUrl: 'views/employee_card.html',
        controller: 'EmployeeCardController'
    };
});