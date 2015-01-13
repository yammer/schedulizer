App.controller('GroupManagerTabController', function ($scope) {

});

App.directive('groupmanagertab', function() {
    return {
        restrict: 'E',
        templateUrl: 'html/partials/groupmanagertab.html',
        controller: 'GroupManagerTabController'
    };
});