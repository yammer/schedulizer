App.run(function($rootScope, $state, $timeout, AuthService, AUTH_EVENTS, NAV_TABS, NESTED_VIEWS, Session) {

    function checkAuthorized(authorizedRoles) {
        if (!AuthService.isAuthorized(authorizedRoles)) {
            if (AuthService.isAuthenticated()) {
                // user is not allowed
                $rootScope.$broadcast(AUTH_EVENTS.notAuthorized);
            } else {
                // user is not logged in
                $rootScope.$broadcast(AUTH_EVENTS.notAuthenticated);
            }
            return false;
        }
        return true;
    }

    function iAmNotAuthorizedToBeHere() {
        // If I am already in an unauthorized state
        var allStates = _.extend(NESTED_VIEWS, NAV_TABS);
        var allStatesArray = Object.keys(allStates).map(function(k){return allStates[k]});
        var currentState = _.find( allStatesArray, function(s) { return s.stateName == $state.current.name; });
        return !checkAuthorized(currentState.data.authorizedRoles);
    }

    $rootScope.$on(AUTH_EVENTS.logoutSuccess, function() {
        if (iAmNotAuthorizedToBeHere()) {
            $state.go(NAV_TABS.group.stateName, {});
        }
    });

    $rootScope.$on(AUTH_EVENTS.authServiceInitialized, function() {

        $rootScope.$on('$stateChangeStart', function (event, next) {
            var authorizedRoles = next.data.authorizedRoles;
            if (!checkAuthorized(authorizedRoles)) {
                event.preventDefault();
            }
        });

        // TODO: figure out a good way of listening to $state.current.name so that we know when it is loaded
        // sometimes the current state name is not yet initialized, hence the timeout
        var numberRetries = 0;
        function retryAuthorizationCheck() {
            numberRetries++;
            if (numberRetries > 20) return;
            try {
                if (iAmNotAuthorizedToBeHere()) {
                    $state.go(NAV_TABS.group.stateName, {});
                }
            } catch (e) {
                $timeout(retryAuthorizationCheck, 20);
            }
        }
        retryAuthorizationCheck();

    });
});

App.config(function($httpProvider) {
  $httpProvider.interceptors.push([
    '$injector',
    function($injector) {
      return $injector.get('AuthInterceptor');
    }
  ]);
});

App.factory('AuthInterceptor', function ($rootScope, $q,
                                         AUTH_EVENTS) {
  return {
    responseError: function (response) {
      $rootScope.$broadcast({
        401: AUTH_EVENTS.notAuthenticated,
        403: AUTH_EVENTS.notAuthorized,
        419: AUTH_EVENTS.sessionTimeout,
        440: AUTH_EVENTS.sessionTimeout
      }[response.status], response);
      return $q.reject(response);
    }
  };
});


App.service('ExtAppSession', function() {
    this.create = function(token, userId) {
        this.token = token;
        this.userId = userId;
    };
    this.destroy = function() {
        this.token = null;
        this.userId = null;
    };
    return this;
});

App.service('Session', function(USER_ROLES) {
    this.create = function (token, userId, userRole, groupsAdmin) {
        this.token = token;
        this.userId = userId;
        this.userRole = userRole;
        this.groupsAdmin = groupsAdmin;
    };
    this.destroy = function(){
        this.token = null;
        this.userId = null;
        this.userRole = USER_ROLES.guest;
        this.groupsAdmin = null;
    };
    this.destroy();
    return this;
});

App.factory('SessionStorage', ['$window', function($window) {
    var SessionStorage = {};

    SessionStorage.save = function(key, value) {
        $window.localStorage.setItem(key, JSON.stringify(value));
    }
    SessionStorage.load = function(key) {
        var value = $window.localStorage.getItem(key);
        if (value) {
            return JSON.parse(value);
        }
        return undefined;
    }
    return SessionStorage;

}]);

function createAuthorizationHeader($http, extAppSession) {
    $http.defaults.headers.common.Authorization = 'SC-AUTH access-token = \"' +
                                                  extAppSession.token +
                                                  '\",' +
                                                  " ext-app-id = \"" +
                                                  extAppSession.userId  +
                                                  "\"";
}

App.factory('AuthService', function($rootScope, $http, $q, $timeout, Session, ExtAppSession, SessionStorage, extAppApi,
                                    AuthorizationResource, Employee, USER_ROLES, AUTH_EVENTS) {
    var authService = {};

    function destroyAuthorizationHeader() {
        $http.defaults.headers.common.Authorization = undefined;
    }

    function getSchedulizerUserStatus() {
        return AuthorizationResource.get().$promise;

    }

    function updateExtAppSession(extAppResponse) {
        if (extAppResponse.authResponse) {
            ExtAppSession.create(extAppResponse.access_token.token, extAppResponse.access_token.user_id);
            SessionStorage.save("extAppSession", ExtAppSession);
        } else {
            ExtAppSession.destroy();
            SessionStorage.save("extAppSession", ExtAppSession);
        }
    }

    function loginSchedulizer(extAppSession) {
        createAuthorizationHeader($http, extAppSession);
        return getSchedulizerUserStatus().then(function(userStatus) {
            if (userStatus.role ==  USER_ROLES.guest) {
                console.error("Something is wrong with the Authorization header. Logged user can not be a guest.");
                return;
            }
            Session.create(extAppSession.token, userStatus.employeeId, userStatus.role, userStatus.groupsAdmin);
            SessionStorage.save('session', Session);
            return userStatus;
        });
    }

    function updateUserInformation(employeeId, extAppResponse) {
        var employee = new Employee({employeeId: employeeId});
        employee.imageUrlTemplate = extAppResponse.user.mugshot_url;
        employee.name = extAppResponse.user.full_name;
        employee.$save();
    }

    function initializeAuthService() {
        extAppApi.getLoginStatus(function(response) {
            updateExtAppSession(response);
            var session = SessionStorage.load('session');
            if (ExtAppSession.token && session) {
                if (session.userRole == USER_ROLES.guest) {
                    Session.create(session.token, session.userId, session.userRole, session.groupsAdmin);
                    $rootScope.$broadcast(AUTH_EVENTS.authServiceInitialized);
                } else {
                    loginSchedulizer(ExtAppSession).then(function(userStatus) {
                        updateUserInformation(userStatus.employeeId, response);
                        $rootScope.$broadcast(AUTH_EVENTS.authServiceInitialized);
                    });
                }
            } else {
                $rootScope.$broadcast(AUTH_EVENTS.authServiceInitialized);
            }

        });
    }

    authService.login = function() {
        var deferredExtAppResponse = $q.defer();
        if (!ExtAppSession.token) {
            extAppApi.login(function(response) {
                updateExtAppSession(response);
                deferredExtAppResponse.resolve(ExtAppSession);
            });
        } else {
            deferredExtAppResponse.resolve(ExtAppSession);
        }
        var deferred = $q.defer();
        deferredExtAppResponse.promise.then(function(extAppSession) {
            loginSchedulizer(extAppSession).then(function(userStatus) {
                deferred.resolve(userStatus);
            });
        });
        return deferred.promise;
    };

    authService.logout = function() {
        var deferred = $q.defer(); // this is done just to be compatible with the login function
        deferred.resolve();
        destroyAuthorizationHeader();
        Session.destroy();
        SessionStorage.save("session", Session);
        return deferred.promise;
    }

    authService.isAuthenticated = function () {
        return !!Session.userId;
    };

    authService.isAuthorized = function(authorizedRoles, groupId) {
        if (!angular.isArray(authorizedRoles)) {
            authorizedRoles = [authorizedRoles];
        }
        if (authorizedRoles.indexOf(Session.userRole) === -1) {
            return false;
        }
        if (groupId == undefined) { // no further conditions
            return true;
        }
        return Session.groupsAdmin && Session.groupsAdmin.indexOf(groupId) !== -1;
    };

    authService.belongsToGroup = function(group) {
        // Are we sure about querying the employeeMap? From employeeFor() function
        // it seems that employeeMap can contain employees that don't belong to the group
        return group.employeeMap != undefined && group.employeeMap[Session.userId] != undefined;
    }

    authService.removeGroupAdminPrivileges = function(groupId) {
        Session.groupsAdmin = _.without(Session.groupsAdmin, groupId);
        SessionStorage.save("session", Session);
    }

    authService.removeGlobalAdminPrivileges = function() {
        if (Session.userRole == USER_ROLES.globalAdmin) {
            Session.userRole = USER_ROLES.user;
        }
        SessionStorage.save("session", Session);
    }

    initializeAuthService();
    return authService;
});

App.run(["SessionStorage", "Session", "ExtAppSession", "$http", function(SessionStorage, Session, ExtAppSession, $http) {
    var extAppSession = SessionStorage.load("extAppSession");
    if(extAppSession && extAppSession.token && extAppSession.userId){
        createAuthorizationHeader($http, extAppSession);
        ExtAppSession.create(extAppSession.token, extAppSession.userId);
    }
    var session = SessionStorage.load("session");
    if (session) {
        Session.create(session.token, session.userId, session.userRole, session.groupsAdmin);
    }
}]);