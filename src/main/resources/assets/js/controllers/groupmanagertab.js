App.controller('GroupManagerTabController', function ($scope, $timeout) {
    $scope.groupList = [{name: "group1"}, {name: "group2"}];
    $scope.$watch('selectedGroup.name', function() {
        $scope.userList = [];
        $scope.loadingUserList = true;
        $timeout(function(){
            if ($scope.selectedGroup.name == "group1") {
                $scope.userList = [
                    {name: "filipe", assignmentList: [{prefix: 'P', count: 10}, {prefix: 'PS', count: 20}, {prefix: 'S', count: 3}] },
                    {name: "bernardo", assignmentList: [{prefix: 'P', count: 2}, {prefix: 'PS', count: 0}, {prefix: 'S', count: 1}]}];
            }
            else {
                $scope.userList = [{name: "ramos", assignmentList: [{prefix: 'P', count: 13}, {prefix: 'PS', count: 10}, {prefix: 'S', count: 2}]},
                    {name: "rufino", assignmentList: [{prefix: 'P', count: 15}, {prefix: 'PS', count: 2}, {prefix: 'S', count: 7}]}];
            }
            $scope.loadingUserList = false;
        }, 500);
    });
});

App.directive('groupmanagertab', function() {
    return {
        restrict: 'E',
        templateUrl: 'html/partials/groupmanagertab.html',
        controller: 'GroupManagerTabController'
    };
});

