App.controller('GroupTabController', function ($scope) {

});

App.directive('grouptab', function() {
    return {
        restrict: 'E',
        scope: { group: '=' },
        templateUrl: 'directives/grouptab.html',
        controller: 'GroupTabController'
    };
});