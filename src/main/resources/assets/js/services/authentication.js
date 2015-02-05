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
    this.create = function (token, userId, userRole) {
        this.token = token;
        this.userId = userId;
        this.userRole = userRole;
    };
    this.destroy = function () {
        this.token = null;
        this.userId = null;
        this.userRole = USER_ROLES.guest;
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

App.factory('AuthService', function ($rootScope, $http, $q, $timeout, Session, YammerSession, yammer,
                                     USER_ROLES, AUTH_EVENTS, SessionStorage) {
    var authService = {};

    function updateYammerSession(yammerResponse) {
        if (yammerResponse.authResponse) {
            YammerSession.create(yammerResponse.access_token.token, yammerResponse.access_token.user_id);
        } else {
            YammerSession.destroy();
        }
    }

    function createStresstimeSession(session) {
        Session.create(session.token, session.userId, session.userRole); // TODO: change to session.userRole
        SessionStorage.save('session', Session);
    }

    yammer.getLoginStatus(function(response) {
        updateYammerSession(response);
        var session = SessionStorage.load('session');
        if(YammerSession.token && session) {
            $rootScope.$apply(function() { // Necessary as it is an async response outside angular
                createStresstimeSession(session); // should be stresstime response (see todo above)
            });
        }
        $rootScope.$broadcast(AUTH_EVENTS.authServiceInitialized);

    });

    authService.login = function () {
        var deferred = $q.defer();
        if (!YammerSession.token) {
            yammer.login(function (response) {
                updateYammerSession(response);
                deferred.resolve(YammerSession);
            });
        }
        else {
            deferred.resolve(YammerSession);
        }
        return deferred.promise.then(function(yammerSession) {
           // TODO: log in our app
           yammerSession.userRole = USER_ROLES.admin; // mock for now
           createStresstimeSession(yammerSession); // should be stresstimeResponse
        });
    };

    authService.logout = function() {
        var deferred = $q.defer(); // this is done just to be compatible with the login function
        deferred.resolve();
        Session.destroy();
        SessionStorage.save("session", Session);
        return deferred.promise;
    }

    authService.isAuthenticated = function () {
        return !!Session.userId;
    };

    authService.isAuthorized = function (authorizedRoles) {
        if (!angular.isArray(authorizedRoles)) {
            authorizedRoles = [authorizedRoles];
        }
        return authorizedRoles.indexOf(Session.userRole) !== -1;
    };
    return authService;
});