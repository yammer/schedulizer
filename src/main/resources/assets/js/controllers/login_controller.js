'use strict';

App.controller('LoginController', function ($scope, $rootScope, $timeout, AUTH_EVENTS, AuthService) {

    $scope.doLogin = function() {
        AuthService.login().then(function() {
            $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
        }, function() {
            $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
        });
    };

    $scope.doLogout = function() {
        AuthService.logout().then(function() {
            $rootScope.$broadcast(AUTH_EVENTS.logoutSuccess);
        });
    };
    $scope.isAuthenticated = AuthService.isAuthenticated;
});