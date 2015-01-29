var services = angular.module('services', ['ngResource']);

var param = function(obj) {
    var query = '', name, value, fullSubName, subName, subValue, innerObj, i;

    for(name in obj) {
        value = obj[name];

        if(value instanceof Array) {
            for(i=0; i<value.length; ++i) {
                subValue = value[i];
                fullSubName = name + '[' + i + ']';
                innerObj = {};
                innerObj[fullSubName] = subValue;
                query += param(innerObj) + '&';
            }
        }
        else if(value instanceof Object) {
            for(subName in value) {
                subValue = value[subName];
                fullSubName = name + '[' + subName + ']';
                innerObj = {};
                innerObj[fullSubName] = subValue;
                query += param(innerObj) + '&';
            }
        }
        else if(value !== undefined && value !== null)
            query += encodeURIComponent(name) + '=' + encodeURIComponent(value) + '&';
    }

    return query.length ? query.substr(0, query.length - 1) : query;
};

var urlencodedTransformRequest = function(data) {
    return angular.isObject(data) && String(data) !== '[object File]' ? param(data) : data;
};

var PREFIX = 'service/';
var SHARED_HEADERS = {'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8'};

services.factory('Group', ['$resource', function($resource) {
    var Group = $resource(PREFIX + 'groups/:group_id', {}, {
        save: {
            method: 'POST',
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        },
        delete: {
            method: 'DELETE',
            params: { group_id: "@id" },
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        }
    });
    Group.prototype.employees = [];
    Group.prototype.assignmentTypes = [];
    return Group;
}]);

services.factory('GroupEmployee', ['$resource', function($resource) {
    return $resource(PREFIX + 'groups/:group_id/employees/:employee_id', {group_id: '@groupId'}, {
        save: {
            method: 'POST',
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        },
        delete: {
            method: 'DELETE',
            params: { employee_id: "@id" },
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        }
    });
}]);


services.factory('AssignmentType', ['$resource', function($resource) {
    return $resource(PREFIX + 'groups/:group_id/assignment-types/:assignment_type_id', {group_id: '@groupId'}, {
        save: {
            method: 'POST',
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        },
        delete: {
            method: 'DELETE',
            params: { assignment_type_id: "@id" },
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        }
    });
}]);

services.factory('AssignableDay', ['$resource', function($resource) {
    return $resource(PREFIX + 'groups/:group_id/assignments/:assignment_id', {group_id: '@groupId'}, {
        save: {
            method: 'POST',
            isArray: true,
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        },
        delete: {
            method: 'DELETE',
            params: { assignment_type_id: "@id" },
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        }
    });
}]);
