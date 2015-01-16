App.controller('AssignableDayController', function ($scope) {

});

App.directive('assignableday', function() {
    return {
        restrict: 'E',
        templateUrl: 'views/assignableday.html',
        controller: 'AssignableDayController'
    };
});