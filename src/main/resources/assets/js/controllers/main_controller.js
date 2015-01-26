App.factory('yammer', ['$window', function($window) {
    var yam = $window.yam;
    if (!yam) throw new Error('Yammer did not load');

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

App.controller("MainController", function(NAV_TABS, $scope, $http, $timeout, $location) {
    /* injecting constants into scope */
    // $route.routes contains the elements of NAV_TABS augmented
    $scope.tabs = angular.copy(NAV_TABS);

    $scope.isActiveTab = function(tab) {
        try {
            return $location.path().indexOf(tab.url) == 0;  // the path should start with url
        } catch (e) {
            return false
        }
    }

    /* defining constants */
    $scope.STATUS = { GUEST: 'guest', USER: 'user', ADMIN: 'admin', GLOBALADMIN: 'globaladmin'}

    /* main functionality */
    $scope.$on("$routeChangeSuccess", function($route) {


        // <TODO: Extract authorization logic to the server!>

//        $scope.userStatus = $scope.STATUS.GUEST;
//
//        yammer.getLoginStatus(function(response) {
//            $timeout(function() {
//                if (response.authResponse) {
//                    $scope.userStatus = $scope.STATUS.GLOBALADMIN;
//                    console.dir(response); //print user information to the console
//                }
//                console.log('TODO: change tabs');
//            }, 0);
//        });
//
//        $scope.isLogged = function() { return $scope.userStatus != $scope.STATUS.GUEST; } ;
//        $scope.isAdmin = function() {
//            return $scope.userStatus == $scope.STATUS.ADMIN || $scope.userStatus == $scope.STATUS.GLOBALADMIN;
//        };
//
//        $scope.dologin = function() {
//
//            if (!$scope.isLogged()) {
//                yammer.login(function (response) {
//                    if (response.authResponse) {
//                        console.dir(response); //print user information to the console
//                    }
//                });
//                $scope.userStatus = $scope.STATUS.GLOBALADMIN;
//                $scope.goToTab($scope.navigation.CALENDAR);
//            }
//            else {
//                $scope.userStatus = $scope.STATUS.GUEST;
//                $scope.goToTab($scope.navigation.GROUPS);
//            }
//        }

        // </TODO>
    });
});
