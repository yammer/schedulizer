'use strict';

/*
    Angularjs initialization
 */
var StresstimeApp = {};

var App = angular.module('StresstimeApp', ['ngAnimate', 'ui.bootstrap', 'services', 'ui.router']);

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
    globalAdmin: 'global',
    admin: 'admin',
    user: 'user',
    guest: 'guest'
}

App.constant('USER_ROLES', USER_ROLES_CONSTANT);

App.constant('NAV_TABS', {
    calendar: {
        title: 'Calendar',                        // Displayed on navbar
        stateName: 'calendar',                     // Name of the state
        url: '/calendar',                       // Link when tab is clicked
        templateUrl: 'views/calendar_tab.html',       // Template loaded into ui-view element
        controller: 'CalendarTabController',     // Controller for this template
        data: {                                 // Which roles can see this state
            authorizedRoles: [USER_ROLES_CONSTANT.globalAdmin, USER_ROLES_CONSTANT.admin, USER_ROLES_CONSTANT.user]
        }
    },
    availability: {
        title: 'Availability',
        stateName: 'availability',
        url: '/availability',
        templateUrl: 'views/availability_tab.html',
        controller: 'AvailabilityTabController',
        data: {
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
        $urlRouterProvider.otherwise(NAV_TABS.calendar.stateName);
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

App.run(function ($rootScope, $state, AuthService, AUTH_EVENTS, NAV_TABS, NESTED_VIEWS) {
    function checkAuthorized(authorizedRoles) {
        if (!AuthService.isAuthorized(authorizedRoles)) {
            if (AuthService.isAuthenticated()) {
                // user is not allowed
                $rootScope.$broadcast(AUTH_EVENTS.notAuthorized);
            } else {
                // user is not logged in
                $rootScope.$broadcast(AUTH_EVENTS.notAuthenticated);
            }
            return false;
        }
        return true;
    }

    $rootScope.$on(AUTH_EVENTS.authServiceInitialized, function() {
        $rootScope.$on('$stateChangeStart', function (event, next) {
            var authorizedRoles = next.data.authorizedRoles;
            if (!checkAuthorized(authorizedRoles)) {
                event.preventDefault();
            }
        });

        // If I am already in an unauthorized state
        var allStates = _.extend(NESTED_VIEWS, NAV_TABS);
        var allStatesArray = Object.keys(allStates).map(function(k){return allStates[k]});
        var currentState = _.find( allStatesArray, function(s) { return s.stateName == $state.current.name; });

        if (!checkAuthorized(currentState.data.authorizedRoles)) {
            $state.go(NAV_TABS.group.stateName, {});
        }
    });
});

App.config(function ($httpProvider) {
  $httpProvider.interceptors.push([
    '$injector',
    function ($injector) {
      return $injector.get('AuthInterceptor');
    }
  ]);
})

App.factory('AuthInterceptor', function ($rootScope, $q,
                                         AUTH_EVENTS) {
  return {
    responseError: function (response) {
      $rootScope.$broadcast({
        401: AUTH_EVENTS.notAuthenticated,
        403: AUTH_EVENTS.notAuthorized,
        419: AUTH_EVENTS.sessionTimeout,
        440: AUTH_EVENTS.sessionTimeout
      }[response.status], response);
      return $q.reject(response);
    }
  };
})

App.factory('DomUtils', ['$rootScope', '$animate', function($rootScope, $animate) {
    return {
        animate: function(type, element) {
            return $animate.addClass(element, type + ' animated').then(function() {
                $rootScope.$apply(function() {
                    $animate.removeClass(element, type + ' animated');
                });
            });
        },
        shakeOnError: function(input) {
            var parent = input.parent();
            parent.addClass('has-error');
            this.animate('shake', input).then(function() {
                parent.removeClass('has-error');
            });
        }
    };
}]);

function resizeCalendar() {
    // TODO
}

$(window).on('resize', function() {
    resizeCalendar();
})

