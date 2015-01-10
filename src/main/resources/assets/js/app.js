'use strict';

var StresstimeApp = {};

var App = angular.module('StresstimeApp', ['ui.bootstrap', 'ngRoute']);


App.controller("MainController", function($scope, $http, $log, $timeout, $modal) {
    $scope.hello = "HelloWorld";
});