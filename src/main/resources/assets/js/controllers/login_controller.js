App.controller('LoginController', function ($scope, $rootScope, $timeout, AUTH_EVENTS, yammer) {
    $scope.isLoggedToYammer = false;
    yammer.getLoginStatus(function(response) {
        $timeout(function() {
            if (response.authResponse && !$scope.isLoggedToYammer) {
                $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
                $scope.isLoggedToYammer = true;
            }
        }, 0);
    });
    $scope.doLogin = function() {
        if (!$scope.isLoggedToYammer) {
            yammer.login(function (response) {
                $timeout(function() {
                    if (response.authResponse && !$scope.isLoggedToYammer) {
                        $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
                        $scope.isLoggedToYammer = true;
                    } else if (!response.authResponse) {
                        $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
                        $scope.isLoggedToYammer = false;
                    }
                });
            });
        }
    }
});