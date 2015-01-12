App.controller("MainController", function($scope, $http, $location, $routeParams, calendar, availability) {
    $scope.CALENDAR = calendar;
    $scope.AVAILABILITY = availability;
    $scope.$on("$routeChangeSuccess", function() {

        $scope.groupList = [{name: "group1"}, {name: "group2"}];
        $scope.navigation = {tab: $routeParams.navigationTab};

        $scope.goToTab = function (tabName) {
            $scope.navigation.tab = tabName;
            $location.path('/' + tabName, false);
        }
    });
});