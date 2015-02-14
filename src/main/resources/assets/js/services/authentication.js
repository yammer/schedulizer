App.run(function ($rootScope, $state, AuthService, AUTH_EVENTS, NAV_TABS, NESTED_VIEWS, Session) {
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
        if (iAmNotAuthorizedToBeHere()) {
            $state.go(NAV_TABS.group.stateName, {});
        }
    });
});

App.config(function ($httpProvider) {
  $httpProvider.interceptors.push([
    '$injector',
    function ($injector) {
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


App.service('YammerSession', function () {
    this.create = function (token, userId) {
        this.token = token;
        this.userId = userId;
    };
    this.destroy = function () {
        this.token = null;
        this.userId = null;
    };
    return this;
});

App.service('Session', function (USER_ROLES) {
    this.create = function (token, userId, userRole, groupsAdmin) {
        this.token = token;
        this.userId = userId;
        this.userRole = userRole;
        this.groupsAdmin = groupsAdmin;
    };
    this.destroy = function () {
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

function createAuthorizationHeader($http, yammerSession) {
    $http.defaults.headers.common.Authorization = 'ST-AUTH access-token = \"' +
                                                  yammerSession.token +
                                                  '\",' +
                                                  " yammer-id = \"" +
                                                  yammerSession.userId  +
                                                  "\"";
}

App.factory('AuthService', function ($rootScope, $http, $q, $timeout, Session, YammerSession, SessionStorage, yammer,
                                     AuthorizationResource, USER_ROLES, AUTH_EVENTS) {
    var authService = {};

    function destroyAuthorizationHeader() {
        $http.defaults.headers.common.Authorization = undefined;
    }

    function getStresstimeUserStatus() {
        return AuthorizationResource.get().$promise;

    }

    function updateYammerSession(yammerResponse) {
        if (yammerResponse.authResponse) {
            YammerSession.create(yammerResponse.access_token.token, yammerResponse.access_token.user_id);
            SessionStorage.save("yammerSession", YammerSession);
        } else {
            YammerSession.destroy();
            SessionStorage.save("yammerSession", YammerSession);
        }
    }

    function loginStresstime(yammerSession) {
        createAuthorizationHeader($http, yammerSession);
        return getStresstimeUserStatus().then(function(userStatus) {
            if (userStatus.role ==  USER_ROLES.guest) {
                console.error("Something is wrong with the Authorization header. Logged user can not be a guest.");
                return;
            }
            Session.create(yammerSession.token, userStatus.employeeId, userStatus.role, userStatus.groupsAdmin);
            SessionStorage.save('session', Session);
            return userStatus;
        });
    }

    function initializeAuthService() {
        yammer.getLoginStatus(function(response) {
            updateYammerSession(response);
            var session = SessionStorage.load('session');
            if(YammerSession.token && session) {
                if (session.userRole == USER_ROLES.guest) {
                    Session.create(session.token, session.userId, session.userRole, session.groupsAdmin);
                    $rootScope.$broadcast(AUTH_EVENTS.authServiceInitialized);
                }
                else {
                    loginStresstime(YammerSession).then(function(userStatus) {
                        $rootScope.$broadcast(AUTH_EVENTS.authServiceInitialized);
                    });
                }
            } else {
                $rootScope.$broadcast(AUTH_EVENTS.authServiceInitialized);
            }

        });
    }

    authService.login = function () {
        var deferredYammerResponse = $q.defer();
        if (!YammerSession.token) {
            yammer.login(function (response) {
                updateYammerSession(response);
                deferredYammerResponse.resolve(YammerSession);
            });
        }
        else {
            deferredYammerResponse.resolve(YammerSession);
        }
        var deferred = $q.defer();
        deferredYammerResponse.promise.then(function(yammerSession) {
            loginStresstime(yammerSession).then(function(userStatus) {
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

    authService.isAuthorized = function (authorizedRoles, groupId) {
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
        return group.employeeMap != undefined && group.employeeMap[Session.userId] != undefined;
    }

    authService.removeGroupAdminPrivileges = function(groupId) {
        Session.groupsAdmin.remove(groupId);
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

App.run(["SessionStorage", "$http", function(SessionStorage, $http) {
    var yammerSession = SessionStorage.load("yammerSession");
    if(yammerSession){
        createAuthorizationHeader($http, yammerSession);
    }
}]);