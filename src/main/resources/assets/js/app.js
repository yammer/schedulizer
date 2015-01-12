'use strict';

var StresstimeApp = {};

var App = angular.module('StresstimeApp', ['ui.bootstrap', 'ngRoute']);


App.controller("MainController", function($scope, $http, $log, $timeout, $modal) {
    $scope.GROUPS = "groups";
    $scope.CALENDAR = "calendar";
    $scope.AVAILABILITY = "availability";
    $scope.groupList = [{name: "group1"}, {name: "group2"}];
    $scope.hello='ola';
});