App.controller('AvailabilityTabController', function ($scope) {

});

App.directive('availabilitytab', function() {
    return {
        restrict: 'E',
        templateUrl: 'directives/availabilitytab.html',
        controller: 'AvailabilityTabController'
    };
});