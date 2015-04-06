'use strict';

/*
    Angularjs initialization
 */
var SchedulizerApp = {};

var App = angular.module('SchedulizerApp', ['ngAnimate', 'ui.bootstrap', 'services', 'calendar-flow', 'ui.router', 'dialogs', 'truncate']);

App.constant('AUTH_EVENTS', {
    loginSuccess: 'auth-login-success',
    loginFailed: 'auth-login-failed',
    logoutSuccess: 'auth-logout-success',
    sessionTimeout: 'auth-session-timeout',
    notAuthenticated: 'auth-not-authenticated',
    notAuthorized: 'auth-not-authorized',
    authServiceInitialized: 'auth-service-initialized'
});

// I did this because in angular you cant inject a constant in a constant and we need USER_ROLES for NAV_TABS
var USER_ROLES_CONSTANT = {
    globalAdmin: 'ADMIN',
    admin: 'MEMBER', // TODO: merge admin and user into a single role called member
    user: 'MEMBER',
    guest: 'GUEST'
}

// Same here... we need to inject this constant into EXT_APP
var EXT_APP_TYPES_CONSTANT = {
    yammer: "yammer",
    facebook: "facebook"
};

App.constant('USER_ROLES', USER_ROLES_CONSTANT);

App.constant('EXT_APP', EXT_APP_TYPES_CONSTANT.facebook);
App.constant('EXT_APP_TYPES', EXT_APP_TYPES_CONSTANT);

App.constant('NAV_TABS', {
    calendar: {
        title: 'My Calendar',                        // Displayed on navbar
        stateName: 'calendar',                     // Name of the state
        url: '/calendar',                       // Link when tab is clicked
        templateUrl: 'views/my_calendar_tab.html',       // Template loaded into ui-view element
        controller: 'MyCalendarTabController',     // Controller for this template
        data: {                                 // Which roles can see this state
            authorizedRoles: [USER_ROLES_CONSTANT.globalAdmin, USER_ROLES_CONSTANT.admin, USER_ROLES_CONSTANT.user]
        }
    },
    group: {
        title: 'Groups',
        stateName: 'groups',                    // The controller handles the default group
        url: '/groups',
        templateUrl: 'views/group_tab.html',
        controller: 'GroupTabController',
        data: {
            authorizedRoles: [USER_ROLES_CONSTANT.globalAdmin, USER_ROLES_CONSTANT.admin,
                              USER_ROLES_CONSTANT.user, USER_ROLES_CONSTANT.guest]
        }
    }
});

App.constant('AVAILABILITY_STATES', [
    {
        label: 'available',
        level: 0,
        title: 'Available',
        glyphicon: 'glyphicon-ok'
    }, {
        label: 'mid-available',
        level: 1,
        title: 'Partially Available',
        glyphicon: 'glyphicon-minus'
    }, {
        label: 'not-available',
        level: 2,
        title: 'Not Available',
        glyphicon: 'glyphicon-remove'
}]
);

App.constant('NESTED_VIEWS', {
    groupsView: {
        title: 'Groups',
        stateName: 'groups.view',
        url: '/:groupId',
        templateUrl: 'views/group_view.html',
        controller: 'GroupViewController',
        data: {
            authorizedRoles: [USER_ROLES_CONSTANT.globalAdmin, USER_ROLES_CONSTANT.admin,
                              USER_ROLES_CONSTANT.user, USER_ROLES_CONSTANT.guest]
        }
    }
});

App.constant('EMPTY_GROUP', {id: undefined, name: "-"});


App.config(['$stateProvider', '$urlRouterProvider', 'NAV_TABS', 'NESTED_VIEWS',
    function($stateProvider, $urlRouterProvider, NAV_TABS, NESTED_VIEWS) {
        //
        // For any unmatched url, redirect to /state1
        $urlRouterProvider.otherwise(NAV_TABS.group.stateName);
        //
        // Now set up the states

        angular.forEach(NAV_TABS, function(tab, id) {
            $stateProvider
                .state(tab.stateName, angular.copy(tab))
        });
        angular.forEach(NESTED_VIEWS, function(view, id) {
            $stateProvider
                .state(view.stateName, angular.copy(view));
        });

}]);

/* Count pending requests */
App.config(function ($provide, $httpProvider) {
     $provide.factory('LoadingInterceptor', function ($q, $rootScope, $timeout) {
         $rootScope.pendingRequests = 0;
         return {
             request: function (config) {
                 $rootScope.pendingRequests++;
                 return config || $q.when(config);
             },
             requestError: function (rejection) {
                // The timeout is to prevent the loading spinner from blinking when there are a lot of
                // successive requests
                $timeout(function(){
                    $rootScope.pendingRequests--;
                }, 200);
                return $q.reject(rejection);
             },
             response: function (response) {
                 $timeout(function(){
                     $rootScope.pendingRequests--;
                 }, 200);
                 return response || $q.when(response);
             },
             responseError: function (rejection) {
                 $timeout(function(){
                     $rootScope.pendingRequests--;
                 }, 200);
                 return $q.reject(rejection);
             }
         };
     });

     $httpProvider.interceptors.push('LoadingInterceptor');
});
