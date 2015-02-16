'use strict';

/*
    Angularjs initialization
 */
var StresstimeApp = {};

var App = angular.module('StresstimeApp', ['ngAnimate', 'ui.bootstrap', 'services', 'ui.router', 'dialogs', 'truncate']);

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

App.constant('USER_ROLES', USER_ROLES_CONSTANT);

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
