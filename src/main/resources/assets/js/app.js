'use strict';

/*
    Angularjs initialization
 */
var StresstimeApp = {};

var App = angular.module('StresstimeApp', ['ui.bootstrap', 'ngRoute']);

/*
 Constants
 */
App.constant('calendar', 'calendar');
App.constant('availability', 'availability');

/*
    Routes
 */
App.config(['$routeProvider', 'calendar', function ($routeProvider, calendar) {

    $routeProvider.when('/:navigationTab', {});
    $routeProvider.otherwise({ redirectTo: calendar });
}]);

/*
    Allow changes in the path without reloading the page
 */
App.run(['$route', '$rootScope', '$location', function ($route, $rootScope, $location) {
    var original = $location.path;
    $location.path = function (path, reload) {
        if (reload === false) {
            var lastRoute = $route.current;
            var un = $rootScope.$on('$locationChangeSuccess', function () {
                $route.current = lastRoute;
                un();
            });
        }
        return original.apply($location, [path]);
    };
}])