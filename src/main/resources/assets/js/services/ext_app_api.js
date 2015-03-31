services.factory('extAppApi', ['$window', 'EXT_APP', 'EXT_APP_TYPES', function($window, EXT_APP, EXT_APP_TYPES) {
    function notImplemented() { throw new Error('Ext App Api not implemented'); }
    var extAppApi = {
        getLoginStatus: notImplemented,
        login: notImplemented,
        autocomplete: notImplemented,
        post: notImplemented
    };

    function formatString(message, tags, mapper) {
        return message.replace(/{(\d+)}/g, function(match, i) {
            return typeof tags[i] != 'undefined'
                ? mapper(tags[i])
                : match;
        });
    }

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
                autocomplete: function(prefix, callback, autocompleteType) {
                    function cacheKey(str) {
                        return autocompleteType + "###" + str;
                    }
                    if (autocompleteCache[cacheKey(prefix)]) {
                        $window.setTimeout(function() {
                            callback(autocompleteCache[cacheKey(prefix)])
                        }, 0); // async because the callback is supposed to be async
                        return;
                    }
                    yam.platform.request({
                        url: "autocomplete/ranked",     //this is one of many REST endpoints that are available
                        method: "GET",
                        data: {
                            "prefix": prefix,
                            "models": autocompleteType + ":20"
                        },
                        success: function (response) { //print message response information to the console
                            if (Object.keys(autocompleteCache).length > 50) {
                                autocompleteCache = {}; // flushing cache if it gets too big
                            }
                            response = {
                                items: response[autocompleteType]
                            };
                            autocompleteCache[cacheKey(prefix)] = response;
                            callback(response);
                        }
                    });
                },
                /**
                 *  Message contains message with {0}, {1) ... representing the tags in the tagList respectively
                 */
                post: function(groupId, message, tagList, callback) {
                    var body = formatString(message, tagList, function(tag) {
                        return "[[user:" + tag.id + "]]";
                    });
                    yam.platform.request({
                        url: "messages.json",
                        method: "POST",
                        data: {
                            "body": body,
                            "group_id": groupId
                        },
                        success: function (response) {
                            console.log(response);
                            callback(response);
                        }
                    });
                }
            }
            break;
        // Integrate with other external apps here by following the examples above
    }
    return extAppApi;

}]);