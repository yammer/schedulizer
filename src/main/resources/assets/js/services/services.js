var services = angular.module('services', ['ngResource']);

// Got from http://victorblog.com/2012/12/20/make-angularjs-http-service-behave-like-jquery-ajax/
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
                if (!value.hasOwnProperty(subName)) continue;
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

var SHARED_HEADERS = {
    'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8'
};

services.factory('yammer', ['$window', function($window) {
    var yam = $window.yam;
    if (!yam) throw new Error('Yammer did not load');
    var autocompleteCache = {};

    return {
        // a me function
        getLoginStatus: function(callback) {
            yam.getLoginStatus(callback);
        },
        login: function(callback){
            yam.platform.login(callback);
        },
        autocomplete: function(prefix, callback) {
            if (autocompleteCache[prefix]) {
                $window.setTimeout(function() {
                    callback(autocompleteCache[prefix])
                }, 0); // async because the callback is supposed to be async
                return;
            }
            yam.platform.request({
                url: "autocomplete/ranked",     //this is one of many REST endpoints that are available
                method: "GET",
                data: {
                    "prefix": prefix,
                    "models": "user:20"
                },
                success: function (user) { //print message response information to the console
                    if (Object.keys(autocompleteCache).length > 50) {
                        autocompleteCache = {}; // flushing cache if it gets too big
                    }
                    autocompleteCache[prefix] = user;
                    callback(user);
                }
            });
        }
    }
}]);

services.factory('Group', ['$resource', 'Employee', function($resource, Employee) {
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
    Group.prototype.employeeMap = {};
    Group.prototype.employeeFor = function(id) {
        return this.employeeMap[id] || (this.employeeMap[id] = Employee.get({employee_id: id}));
    }

    Group.prototype.assignmentTypes = [];
    Group.prototype.assignmentTypeFor = function(id) {
        return _.find(this.assignmentTypes, function(assignmentType) {
            return assignmentType.id == id;
        });
    };

    Group.prototype.addAssignmentType = function(assignmentType) {
        this.setAssignmentTypes(_.union(this.assignmentTypes, [assignmentType]));
    }

    Group.prototype.setAssignmentTypes = function(assignmentTypes) {
        this.assignmentTypes = _.chain(assignmentTypes)
            .sortBy(function(a) {return a.name.toLowerCase();})
            .each(function(a, i) {
                a.index = i;
            })
            .value();
    }

    Group.prototype.addEmployee = function(employee) {
        // TODO: Fix flash when adding new employee in different order
        this.employees.push(employee);
        this.employeeMap[employee.id] = employee;
    }

    return Group;
}]);

services.factory('GroupEmployee', ['$resource', function($resource) {
    var GroupEmployee = $resource(PREFIX + 'groups/:group_id/employees/:employee_id', {group_id: '@groupId'}, {
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

    GroupEmployee.prototype.statistics = {}; // {assignmentTypeId: {assignmentType, assignmentTypeId, count}}
    GroupEmployee.prototype.statisticsFor = function(assignmentTypeId) {
        return _.find(this.statistics, function(s, id) {
            return id == assignmentTypeId;
        })
    }

    return GroupEmployee;
}]);

services.factory('Employee', ['$resource', function($resource) {
    return $resource(PREFIX + 'employees/:employee_id', {employee_id: '@employeeId'}, {});
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
    var AssignableDay = $resource(PREFIX + 'groups/:group_id/assignments/:assignment_id', {group_id: '@groupId'}, {
        save: {
            method: 'POST',
            isArray: true,
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        },
        delete: {
            method: 'DELETE',
            params: { assignment_id: "@id" },
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        }
    });

    AssignableDay.prototype.getDate = function() {
        return Date.fromISOLocalString(this.date);
    };

    return AssignableDay;
}]);

services.factory('DayRestriction', ['$resource', function($resource) {
    var DayRestriction = $resource(PREFIX + 'employees/:employee_id/restrictions/:day_restriction_id', {employee_id: '@employeeId'}, {
        save: {
            method: 'POST',
            isArray: true,
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        },
        delete: {
            method: 'DELETE',
            params: { day_restriction_id: "@id" },
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        }
    });

    DayRestriction.prototype.getDate = function() {
        return Date.fromISOLocalString(this.date);
    };

    return DayRestriction;
}]);

services.factory('AuthorizationResource', ['$resource', function($resource) {
    return $resource(PREFIX + 'current');
}]);

services.factory('AssignmentStats', ['$resource', function($resource) {
    return $resource(PREFIX + 'groups/:group_id/assignments/stats', {group_id: '@groupId'}, {
        query: {
            isArray: false
        }
    });
}]);

services.factory('AdminsResource', ['$resource', function($resource) {
    var AssignableDay = $resource(PREFIX + 'groups/:group_id/admins/:employee_id',  {employee_id: "@employeeId", group_id: "@groupId"}, {
        save: {
            method: 'POST',
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        },
        delete: {
            method: 'DELETE',
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        }
    });

    AssignableDay.prototype.getDate = function() {
        return Date.fromISOLocalString(this.date);
    }

    return AssignableDay;
}]);

services.factory('GlobalAdminsResource', ['$resource', function($resource) {
    var GlobalAdminsResource = $resource(PREFIX + 'employees/admins/:employee_id',  { employee_id: "@id"}, {
        save: {
            method: 'POST',
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        },
        delete: {
            method: 'DELETE',
            transformRequest: [urlencodedTransformRequest],
            headers: SHARED_HEADERS
        }
    });

    return GlobalAdminsResource;
}]);

services.factory('EmployeeAssignmentsResource', ['$resource', function($resource) {
    var EmployeeAssignmentsResource = $resource(PREFIX + 'employees/:employee_id/assignments', {}, {});

    EmployeeAssignmentsResource.prototype.getDate = function() {
        return Date.fromISOLocalString(this.date);
    };

    EmployeeAssignmentsResource.prototype.getFullName = function() {
        return this.group.name + " - " + this.assignmentTypeName;
    }

    return EmployeeAssignmentsResource;
}]);