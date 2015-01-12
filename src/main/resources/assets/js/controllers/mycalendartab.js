App.controller('MyCalendarTabController', function ($scope) {

});

App.directive('mycalendartab', function() {
    return {
        restrict: 'E',
        templateUrl: 'html/partials/mycalendartab.html',
        controller: 'MyCalendarTabController'
    };
});