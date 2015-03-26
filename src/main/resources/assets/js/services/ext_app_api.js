services.factory('extAppApi', ['$window', 'EXT_APP', 'EXT_APP_TYPES', function($window, EXT_APP, EXT_APP_TYPES) {
    function notImplemented() { throw new Error('Ext App Api not implemented'); }
    var extAppApi = {
        getLoginStatus: notImplemented,
        login: notImplemented,
        autocomplete: notImplemented
    };

    switch (EXT_APP) {
        case EXT_APP_TYPES.yammer:
            var yam = $window.yam;
            if (!yam) throw new Error('Could not load yammer api');
            var autocompleteCache = {};

            extAppApi =  {
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
            break;
        // Integrate with other external apps here by following the examples above
    }
    return extAppApi;

}]);