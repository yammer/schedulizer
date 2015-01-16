App.controller('AssignableDayController', function ($scope) {

});

App.directive('assignableday', function() {
    return {
        restrict: 'E',
        templateUrl: 'html/partials/assignableday.html',
        controller: 'AssignableDayController'
    };
});