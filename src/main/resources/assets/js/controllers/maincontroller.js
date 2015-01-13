App.controller("MainController", function($scope, $http, $location, $routeParams,
                                          navigation) {
    /* injecting constants into scope */
    $scope.navigation = {};
    $scope.navigation.GROUPMANAGER = navigation.groupmanager;
    $scope.navigation.CALENDAR = navigation.calendar;
    $scope.navigation.AVAILABILITY = navigation.availability;
    $scope.navigation.GROUPS = navigation.groups;
    $scope.navigation.LOGIN = navigation.login;

    /* defining constants */
    $scope.STATUS = { GUEST: 'guest', USER: 'user', ADMIN: 'admin', GLOBALADMIN: 'globaladmin'}

    /* main functionality */
    $scope.$on("$routeChangeSuccess", function() {
        $scope.navigation.tab = $routeParams.navigationTab;

        $scope.userStatus = "guest";
        $scope.isLogged = function() { return $scope.userStatus != $scope.STATUS.GUEST; } ;
        $scope.isAdmin = function() {
            return $scope.userStatus == $scope.STATUS.ADMIN || $scope.userStatus == $scope.STATUS.GLOBALADMIN;
        } ;

        $scope.dologin = function(user) {
            $scope.userStatus = $scope.STATUS.GLOBALADMIN;
            $scope.goToTab($scope.navigation.CALENDAR);
        }

        $scope.goToTab = function (tabName) {
            if(tabName === $scope.navigation.LOGIN && $scope.isLogged()) {
                $scope.userStatus = $scope.STATUS.GUEST;
            }

            $scope.navigation.tab = tabName;

            $location.path('/' + tabName, false);
        }

        $scope.$watch('navigation.tab', function() {
            // Check if user has access to the navigation tab
            if (!$scope.isLogged() && $scope.navigation.tab != $scope.navigation.GROUPS &&
                $scope.navigation.tab != $scope.navigation.LOGIN) {
                    $scope.goToTab($scope.navigation.GROUPS);
            }
            else if (!$scope.isAdmin()) {
                if ($scope.navigation.tab != $scope.navigation.GROUPS &&
                    $scope.navigation.tab != $scope.navigation.CALENDAR &&
                    $scope.navigation.tab != $scope.navigation.AVAILABILITY &&
                    $scope.navigation.tab != $scope.navigation.LOGIN) {
                        $scope.goToTab($scope.navigation.CALENDAR);
                }
            }
        });
    });
});