App.controller("MainController", function(NAV_TABS, $scope, $http, $timeout, $location, $dialogs, AuthService, USER_ROLES, $rootScope) {
    $scope.pendingRequests = function() {
        return $rootScope.pendingRequests;
    }
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

    $rootScope.isGlobalAdmin = function() {
        return $rootScope.isAuthorized(USER_ROLES.globalAdmin);
    }

    $rootScope.isGroupAdmin = function(group) {
        if (group == null) return false;
        if ($scope.isAuthorized($scope.userRoles.globalAdmin)) { return true; }
        return $scope.isAuthorized($scope.userRoles.admin, group.id);
    }

    $rootScope.isGroupMember = function(group) {
        if (group == null) return false;
        return $scope.isAuthorized([$scope.userRoles.user, $scope.userRoles.globalAdmin]) && AuthService.belongsToGroup(group);
    }

    $rootScope.isGroupMemberOrGlobalAdmin = function(group) {
        return $rootScope.isGroupMember(group) || $rootScope.isGlobalAdmin();
    }

    $scope.globalAdminModal = function() {
        dlg = $dialogs.create('/views/global_admin_modal.html','GlobalAdminModalController',{},{key: false, back: 'static'});
        dlg.result.then(function(changed) {
            if (changed) {
                $rootScope.$broadcast("global-admins-changed");
            }
        });
    }

});
