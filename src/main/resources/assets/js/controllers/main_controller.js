App.controller("MainController", function(NAV_TABS, $scope, $http, $timeout, $location) {
    /* injecting constants into scope */
    $scope.tabs = angular.copy(NAV_TABS);

    $scope.isActiveTab = function(tab) {
        try {
            return $location.path().indexOf(tab.url) == 0;  // the path should start with url
        } catch (e) {
            return false
        }
    }
});
