App.controller('EmployeeCardController', function($scope, $rootScope, $interval) {
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

    $scope.employeeNameStyle = {width: '0px'};

    function resizeEmployeeName() {
        if ($scope.employeeNameContainer == null || $scope.employeeName == null) return;
        var w = $scope.employeeNameContainer.innerWidth();
        var n = _.size($scope.employee.statistics);
        //noinspection JSValidateTypes
        var b = $scope.employeeNameContainer.find('.mini-assignment-type-label').outerWidth(true) || 25;
        $scope.employeeNameStyle.width = (w - b * n - 50) + 'px';
    }


    var containerInterval = $interval(function() {
        if ($scope.employeeNameContainer && $scope.employeeNameContainer.innerWidth() > 0) {
            $interval.cancel(containerInterval);
            resizeEmployeeName();
        }
    }, 100);

    $(window).resize(function() {
        $scope.$apply(resizeEmployeeName);
    });
});

App.directive('employeeCard', function() {
    return {
        restrict: 'E',
        scope: {
            employee: "=",
            group: "=",
            remove: "&",
            toggleAdmin: "=",
            onClick: "=",
            selectEmployee: "="
        },
        templateUrl: 'views/employee_card.html',
        controller: 'EmployeeCardController'
    };
});