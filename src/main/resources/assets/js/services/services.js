var services = angular.module('services', ['ngResource']);

services.factory('Group', ['$resource', function($resource) {
    return $resource('mock/groups.json');
}]);