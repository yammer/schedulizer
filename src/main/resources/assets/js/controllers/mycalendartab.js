App.controller('MyCalendarTabController', function ($scope) {

});

App.directive('mycalendartab', function() {
    return {
        restrict: 'E',
        templateUrl: 'directives/mycalendartab.html',
        controller: 'MyCalendarTabController'
    };
});