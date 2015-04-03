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
                // A me function
                getLoginStatus: function(callback) {
                    yam.getLoginStatus(function(response) {
                        callback({
                            access_token: response.access_token.token,
                            user: {
                                id: response.access_token.user_id,
                                photo_url: response.user.mugshot_url,
                                full_name: response.user.full_name
                            }
                        });
                    });
                },
                /* Should return the access token */
                login: function(callback){
                    yam.platform.login(function(response) {
                        callback({
                            access_token: response.access_token.token,
                            user: {
                                id: response.access_token.user_id
                            }
                        });
                    });
                },
                /*  Don't worry about caching... it is handled later */
                autocomplete: function(prefix, callback, autocompleteType) {
                    yam.platform.request({
                        url: "autocomplete/ranked",     //this is one of many REST endpoints that are available
                        method: "GET",
                        data: {
                            "prefix": prefix,
                            "models": autocompleteType + ":20"
                        },
                        success: function (response) { //print message response information to the console
                            callback({
                                items: response[autocompleteType]
                            });
                        }
                    });
                },
                /**
                 *  Message contains the text with {0}, {1) ... representing the tags in the tagList respectively
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
                            var url = "";
                            if (response.messages && response.messages.length > 0 && response.messages[0].web_url) {
                                url = response.messages[0].web_url;
                            }
                            callback(url);
                        }
                    });
                }
            }
            break;
        // Integrate with other external apps here by following the examples above
    }

    // Insert cache for the autocomplete
    var innerAutocomplete = extAppApi.autocomplete;
    extAppApi.autocomplete = function(prefix, callback, autocompleteType) {
        function cacheKey(str) {
            return autocompleteType + "###" + str;
        }
        if (autocompleteCache[cacheKey(prefix)]) {
            $window.setTimeout(function() {
                callback(autocompleteCache[cacheKey(prefix)])
            }, 0); // async because the callback is supposed to be async
            return;
        }
        innerAutocomplete(prefix, function(response){
            if (Object.keys(autocompleteCache).length > 50) {
                autocompleteCache = {}; // flushing cache if it gets too big
            }
            autocompleteCache[cacheKey(prefix)] = response;
            callback(response);
        }, autocompleteType);
    }

    return extAppApi;

}]);