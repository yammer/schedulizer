App.controller('LoginTabController', function ($scope) {
    $scope.dologin = function() {
        $scope.onlogin("Luiz");
    };
});

App.directive('logintab', function() {
    return {
        restrict: 'E',
        scope: { onlogin: "=" },
        templateUrl: 'html/partials/logintab.html',
        controller: 'LoginTabController'
    };
});