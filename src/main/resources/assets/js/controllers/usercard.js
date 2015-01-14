App.controller('UserCardController', function ($scope) {
});

App.directive('usercard', function() {
    return {
        restrict: 'E',
        scope: { user: "=" },
        templateUrl: 'html/partials/usercard.html',
        controller: 'UserCardController'
    };
});