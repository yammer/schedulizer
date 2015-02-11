App.controller("MainController", function(NAV_TABS, $scope, $http, $timeout, $location, AuthService, USER_ROLES, $rootScope) {
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
    $rootScope.isAuthorized = AuthService.isAuthorized;

    $rootScope.isGroupAdmin = function(group) {
        if (group == undefined) return false;
        if ($scope.isAuthorized($scope.userRoles.globalAdmin)) { return true; }
        return $scope.isAuthorized($scope.userRoles.admin, group.id);
    }

    $rootScope.isGroupMember = function(group) {
        if (group == undefined) return false;
        if ($scope.isAuthorized($scope.userRoles.globalAdmin)) { return true; }
        return $scope.isAuthorized([$scope.userRoles.user, $scope.userRoles.globalAdmin]) && AuthService.belongsToGroup(group);
    }

});
