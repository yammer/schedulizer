App.controller('UserCardController', function ($scope) {
});

App.directive('usercard', function() {
    return {
        restrict: 'E',
        scope: { user: "=" },
        templateUrl: 'views/usercard.html',
        controller: 'UserCardController'
    };
});