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

App.factory('AuthService', function ($rootScope, $http, $q, $timeout, Session, YammerSession, yammer,
                                     USER_ROLES, AUTH_EVENTS) {
    var authService = {};

    function updateYammerSession(yammerResponse) {
        if (yammerResponse.authResponse) {
            YammerSession.create(yammerResponse.access_token.token, yammerResponse.access_token.user_id);
        } else {
            YammerSession.destroy();
        }
    }

    function createStresstimeSession(stresstimeResponse) {
         Session.create(stresstimeResponse.token, stresstimeResponse.userId, USER_ROLES.globalAdmin);
    }

    yammer.getLoginStatus(function(response) {
        updateYammerSession(response);
        // TODO: check cookies to see if user is logged in our app
        if(YammerSession.token) {
            createStresstimeSession(YammerSession) // should be stresstime response (see todo above)
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
           createStresstimeSession(yammerSession); // should be stresstimeResponse
        });
    };

    authService.logout = function() {
        var deferred = $q.defer(); // this is done just to be compatible with the login function
        deferred.resolve();
        Session.destroy();
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