'use strict';

/*
    Angularjs initialization
 */
var StresstimeApp = {};

var App = angular.module('StresstimeApp', ['ui.bootstrap', 'services', 'ui.router']);

App.constant('NAV_TABS', {
    calendar: {
        title: 'Calendar',                        // Displayed on navbar
        stateName: 'calendar',                     // Name of the state
        url: '/calendar',                       // Link when tab is clicked
        templateUrl: 'views/calendar_tab.html',       // Template loaded into ui-view element
        controller: 'CalendarTabController'     // Controller for this template
    },
    availability: {
        title: 'Availability',
        stateName: 'availability',
        url: '/availability',
        templateUrl: 'views/availability_tab.html',
        controller: 'AvailabilityTabController'
    },
    group: {
        title: 'Groups',
        stateName: 'groups',                    // The controller handles the default group
        url: '/groups',
        templateUrl: 'views/group_tab.html',
        controller: 'GroupTabController'
    }
});

App.constant('NESTED_VIEWS', {
    groupsView: {
        title: 'Groups',
        stateName: 'groups.view',
        url: '/:groupId',
        templateUrl: 'views/group_view.html',
        controller: 'GroupViewController'
    }
})

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

App.directive('stName', function() {
    return {
        restrict: 'A',
        scope: {
            stName: '='
        },
        link: function(scope, element, attrs) {
            scope.stName = element;
        }
    }
})

function resizeCalendar() {
    // TODO
}

$(window).on('resize', function() {
    resizeCalendar();
})

