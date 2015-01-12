'use strict';

var StresstimeApp = {};

var App = angular.module('StresstimeApp', ['ui.bootstrap', 'ngRoute']);

App.directive('mycalendartab', function() {
    return {
        restrict: 'E',
        templateUrl: 'directives/mycalendartab.html',
        controller: 'MyCalendarTabController'
    };
});

App.directive('availabilitytab', function() {
    return {
        restrict: 'E',
        templateUrl: 'directives/availabilitytab.html',
        controller: 'AvailabilityTabController'
    };
});

App.directive('grouptab', function() {
    return {
        restrict: 'E',
        scope: { group: '=' },
        templateUrl: 'directives/grouptab.html',
        controller: 'GroupTabController'
    };
});

