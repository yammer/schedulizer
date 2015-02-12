App.directive('scroller', function($timeout) {
    return {
        restrict: 'A',
        scope: {
            nextChunkMethod: "=",
            previousChunkMethod: "=", // return the height of the chunk if you don't want a flash on the screen,
            onScroll: "=",
            numberOfLoadedChunksPerScroll: "="
        },
        // Make scroll even if it does not have scroll (increase height to test)
        link: function($scope, element, attrs) {
            rawElement = element[0];
            if ($scope.numberOfLoadedChunksPerScroll == undefined) {
                $scope.numberOfLoadedChunksPerScroll = 1;
            }

            element.bind('scroll', function () {

                $scope.onScroll(
                    rawElement.scrollTop,
                    rawElement.scrollTop + rawElement.offsetHeight - 5,
                    rawElement.scrollHeight);

                // + 5 error margin
                if ((rawElement.scrollTop + rawElement.offsetHeight + 5) >= rawElement.scrollHeight
                        && $scope.nextChunkMethod) {
                    // User has scrolled down and we should fetch next chunk
                    $scope.$apply(function() {
                        for(var i = 0; i < $scope.numberOfLoadedChunksPerScroll; i++) {
                            $scope.nextChunkMethod();
                        }
                    });
                }
                if (rawElement.scrollTop == 0 && $scope.previousChunkMethod){
                    // User has scrolled up and we should fetch previous chunk
                    $scope.$apply(function() {
                        var chunkSize = 0;
                        for(var i = 0; i < $scope.numberOfLoadedChunksPerScroll; i++) {
                            chunkSize += $scope.previousChunkMethod();
                        }
                        if (chunkSize) { // If the provided function gave us the height, GOOD
                            rawElement.scrollTop = chunkSize;
                        } else { // Otherwise we have to calculate and flash the screen =(
                            var previousHeight = rawElement.scrollHeight;
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
