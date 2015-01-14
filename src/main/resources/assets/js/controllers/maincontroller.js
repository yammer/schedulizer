App.factory('yammer', ['$window', function($window) {

    var yam = $window.yam;

    if(!yam) throw new Error('Yammer did not load');

    return {
        // a me function
        getLoginStatus: function(callback) {
            yam.getLoginStatus(callback);
        },
        login: function(callback){
            yam.platform.login(callback);
        }
    }
}]);

App.controller("MainController", function($scope, $http, $timeout, $location, $routeParams,
                                          navigation, yammer) {
    /* injecting constants into scope */
    $scope.navigation = {};
    $scope.navigation.GROUPMANAGER = navigation.groupmanager;
    $scope.navigation.CALENDAR = navigation.calendar;
    $scope.navigation.AVAILABILITY = navigation.availability;
    $scope.navigation.GROUPS = navigation.groups;

    /* defining constants */
    $scope.STATUS = { GUEST: 'guest', USER: 'user', ADMIN: 'admin', GLOBALADMIN: 'globaladmin'}

    /* main functionality */
    $scope.$on("$routeChangeSuccess", function() {
        $scope.navigation.tab = $routeParams.navigationTab;

        $scope.userStatus = $scope.STATUS.GUEST;

        $scope.checkNavTab = function() {
            // Check if user has access to the navigation tab
            if (!$scope.isLogged() && $scope.navigation.tab != $scope.navigation.GROUPS) {
                $scope.goToTab($scope.navigation.GROUPS);
            }
            else if (!$scope.isAdmin()) {
                if ($scope.navigation.tab != $scope.navigation.GROUPS &&
                    $scope.navigation.tab != $scope.navigation.CALENDAR &&
                    $scope.navigation.tab != $scope.navigation.AVAILABILITY) {
                    $scope.goToTab($scope.navigation.CALENDAR);
                }
            }
        }


        yammer.getLoginStatus(function (response) {
            $timeout(function () {
                if (response.authResponse) {
                    $scope.userStatus = $scope.STATUS.GLOBALADMIN;
                    console.dir(response); //print user information to the console
                }
                $scope.checkNavTab();
            }, 0);
        });

        $scope.isLogged = function() { return $scope.userStatus != $scope.STATUS.GUEST; } ;
        $scope.isAdmin = function() {
            return $scope.userStatus == $scope.STATUS.ADMIN || $scope.userStatus == $scope.STATUS.GLOBALADMIN;
        } ;

        $scope.dologin = function() {

            if (!$scope.isLogged()) {
                yammer.login(function (response) {
                    if (response.authResponse) {
                        console.dir(response); //print user information to the console
                    }
                });
                $scope.userStatus = $scope.STATUS.GLOBALADMIN;
                $scope.goToTab($scope.navigation.CALENDAR);
            }
            else {
                $scope.userStatus = $scope.STATUS.GUEST;
                $scope.goToTab($scope.navigation.GROUPS);
            }
        }

        $scope.goToTab = function (tabName) {
            $scope.navigation.tab = tabName;
            $location.path('/' + tabName, false);
        }
    });
});