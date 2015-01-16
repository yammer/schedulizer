'use strict';

/*
    Angularjs initialization
 */
var StresstimeApp = {};

var App = angular.module('StresstimeApp', ['ui.bootstrap', 'ngRoute']);

App.constant('NAV_TABS', {
    calendar: {
        title: 'Calendar',                        // Displayed on navbar
        href: 'calendar',                         // Link when tab is clicked
        route:  '/calendar',                      // Route that if matched triggers the controller
        templateUrl: 'views/calendar_tab.html',       // Template loaded into ng-view element
        controller: 'CalendarTabController'     // Controller for this template
    },
    availability: {
        title: 'Availability',
        href: 'availability',
        route: '/availability',
        templateUrl: 'views/availability_tab.html',
        controller: 'AvailabilityTabController'
    },
    group: {
        title: 'Groups',
        href: 'group/default',                    // The controller handles the default group
        route: '/group/:groupId',
        templateUrl: 'views/group_tab.html',
        controller: 'GroupTabController'
    }
});

App.config(['$routeProvider', 'NAV_TABS', function($routeProvider, NAV_TABS) {
    angular.forEach(NAV_TABS, function(tab, id) {
        $routeProvider.when(tab.route, angular.copy(tab));
    });
    $routeProvider.otherwise({redirectTo: NAV_TABS.calendar.route});
}]);
