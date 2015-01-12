App.controller('GroupTabController', function ($scope) {

});

App.directive('grouptab', function() {
    return {
        restrict: 'E',
        scope: { group: '=' },
        templateUrl: 'html/partials/grouptab.html',
        controller: 'GroupTabController'
    };
});