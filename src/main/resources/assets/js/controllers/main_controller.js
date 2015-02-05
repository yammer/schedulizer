App.controller("MainController", function(NAV_TABS, $scope, $http, $timeout, $location, AuthService, USER_ROLES) {
    /* injecting constants into scope */
    $scope.tabs = angular.copy(NAV_TABS);

    $scope.isActiveTab = function(tab) {
        try {
            return $location.path().indexOf(tab.url) == 0;  // the path should start with url
        } catch (e) {
            return false
        }
    }

    $scope.userRoles = USER_ROLES;
    $scope.isAuthorized = AuthService.isAuthorized;

    $scope.isGroupAdmin = function(group) {
        if ($scope.isAuthorized($scope.userRoles.globalAdmin)) { return true; }
        if (!$scope.isAuthorized($scope.userRoles.admin)) { return false; }
        // TODO: Check if is this group's admin
        return true;
    }

});
