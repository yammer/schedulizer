App.controller('AvailabilityTabController', function ($scope) {

});

App.directive('availabilitytab', function() {
    return {
        restrict: 'E',
        templateUrl: 'html/partials/availabilitytab.html',
        controller: 'AvailabilityTabController'
    };
});