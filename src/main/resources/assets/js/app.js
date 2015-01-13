'use strict';

/*
    Angularjs initialization
 */
var StresstimeApp = {};

var App = angular.module('StresstimeApp', ['ui.bootstrap', 'ngRoute']);

/*
 Constants
 */
App.constant('navigation', {
    calendar: 'calendar',
    availability: 'availability',
    groups: 'groups',
    groupmanager: 'groupmanager',
});


/*
    Routes
 */
App.config(['$routeProvider', 'navigation', function ($routeProvider, navigation) {

    $routeProvider.when('/:navigationTab', {});
    $routeProvider.otherwise({ redirectTo: navigation.calendar });
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