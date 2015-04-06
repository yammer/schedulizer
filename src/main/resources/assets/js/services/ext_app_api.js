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

    var autocompleteCache = {};

    switch (EXT_APP) {
        case EXT_APP_TYPES.yammer:
            var yam = $window.yam;
            if (!yam) throw new Error('Could not load yammer api');
            extAppApi =  {
                // A me function
                getLoginStatus: function(callback) {
                    yam.getLoginStatus(function(response) {
                        if(response.authResponse == undefined) {
                            callback({});
                            return;
                        }
                        callback({
                            access_token: response.access_token.token,
                            user: {
                                id: response.access_token.user_id,
                                photo: response.user.mugshot_url,
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
        case EXT_APP_TYPES.facebook:
            var fb = $window.FB;
            if (!fb) throw new Error('Could not load facebook api');
            fb.init({
                appId      : '617709521696922',
                xfbml      : true,
                version    : 'v2.3'
            });
            extAppApi =  {
                // A me function
                getLoginStatus: function(callback) {
                    fb.getLoginStatus(function(response) {
                        console.log(response);
                        if(response.authResponse == undefined) {
                            callback({});
                            return;
                        }
                        fb.api('/me', {
                            fields: ['name', 'picture']
                        }, function(meResponse) {
                            console.log(meResponse);
                            callback({
                                access_token: response.authResponse.accessToken,
                                user: {
                                    id: response.authResponse.userID,
                                    photo: meResponse.picture.data.url,
                                    full_name: meResponse.name
                                }
                            });
                        });

                    });
                },
                /* Should return the access token */
                login: function(callback){
                    fb.login(function(response){
                        callback({
                            access_token: response.authResponse.accessToken,
                            user: {
                                id: response.authResponse.userID
                            }
                        });
                    }, {
                        scope: ['user_friends']
                    });
                },
                /*  Don't worry about caching... it is handled later */
                autocomplete: function(prefix, callback, autocompleteType) {
                    if (autocompleteType == 'user') {
                        function getFriends(callback, offset) {
                            var OFFSET_DIFF = 100;
                            if (offset == undefined) {
                                offset = 0;
                            }
                            fb.api('/me/friends', {
                                limit: OFFSET_DIFF,
                                offset: offset,
                                fields: [
                                    'name',
                                    'picture{url}',
                                    'id'
                                ]
                            }, function (response) {
                                if (response.data == undefined || response.data.length == 0) {
                                    fb.api('/me', {
                                        fields: ['id', 'name', 'picture']
                                    }, function (meResponse) {
                                        callback([{
                                            photo: meResponse.picture.data.url,
                                            full_name: meResponse.name,
                                            id: meResponse.id
                                        }]);
                                    });
                                    return;
                                }
                                getFriends(function (friends) {
                                    callback(_.union(response.data.map(function (el) {
                                        el.full_name = el.name;
                                        return el;
                                    }), friends));
                                }, offset + OFFSET_DIFF);
                            });
                        }

                        getFriends(function (friends) {
                            callback({
                                items: friends
                            });
                        });
                    } else if (autocompleteType == 'group') {
                        // Facebook's API does not allow this feature
                        callback({
                            items: []
                        });
                    }
                },
                /**
                 *  Facebook's API does not allow this feature
                 */
                post: undefined
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