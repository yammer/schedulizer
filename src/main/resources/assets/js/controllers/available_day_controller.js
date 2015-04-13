'use strict';

App.controller('AvailableDayController', function ($scope) {

});

App.directive('availableday', function() {
    return {
        restrict: 'E',
        templateUrl: 'html/partials/availableday.html',
        controller: 'AvailableDayController'
    };
});