App.directive('scroller', function () {
    return {
        restrict: 'A',
        scope: {
            nextChunkMethod: "&",
            previousChunkMethod: "&",
            numberOfLoadedChunksPerScroll: "="
        },
        link: function ($scope, elem, attrs) {
            rawElement = elem[0];
            if ($scope.numberOfLoadedChunksPerScroll == undefined) {
                $scope.numberOfLoadedChunksPerScroll = 1;
            }
            elem.bind('scroll', function () {
                if((rawElement.scrollTop + rawElement.offsetHeight+5) >= rawElement.scrollHeight &&
                    $scope.nextChunkMethod){
                    $scope.$apply( function() {
                        for(var i = 0; i < $scope.numberOfLoadedChunksPerScroll; i++) {
                            $scope.nextChunkMethod();
                        }
                    });
                }
                if(rawElement.scrollTop == 0 &&
                    $scope.previousChunkMethod){
                    $scope.$apply(function() {
                        var chunkSize = 0;
                        for(var i = 0; i < $scope.numberOfLoadedChunksPerScroll; i++) {
                            chunkSize += $scope.previousChunkMethod();
                        }
                        if(chunkSize) {
                            rawElement.scrollTop = chunkSize;
                        }
                        else {
                            setTimeout(function () {
                                rawElement.scrollTop = rawElement.scrollHeight - previousHeight;
                            }, 0);
                        }
                    });

                }
            });
        }
    };
});
