var guid = (function() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return function() {
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    };
})();
$.event.props.push('dataTransfer');
App.directive('ngDraggable', ['$rootScope', function($rootScope) {
    return {
        restrict: 'A',
        scope: {
            ghostElement: '@'
        },
        link: function(scope, el, attrs, controller) {
            angular.element(el).attr("draggable", "true");

            var id = angular.element(el).attr("id");
            if (!id) {
                id = guid()
                angular.element(el).attr("id", id);
            }

            el.bind("dragstart", function(e) {
                if (scope.ghostElement) {
                    try {
                        var img = $(el).find(scope.ghostElement)[0];
                        var w = img.offsetWidth;
                        var h = img.offsetHeight;
                        e.dataTransfer.setDragImage(img, w/2, h/2);
                    } catch (exception) {

                    }

                }
                e.dataTransfer.setData('text', id);
                $rootScope.$emit("DRAG-START");
            });

            el.bind("dragend", function(e) {
                $rootScope.$emit("DRAG-END");
            });
        }
    }
}]);

App.directive('ngDroppable', ['$rootScope', function($rootScope) {
    return {
        restrict: 'A',
        scope: {
            ngOnDrop: '='
        },
        link: function($scope, el, attrs, controller) {
            var id = angular.element(el).attr("id");
            if (!id) {
                id = guid()
                angular.element(el).attr("id", id);
            }

            el.bind("dragover", function(e) {
                if (e.preventDefault) {
                    e.preventDefault(); // Necessary. Allows us to drop.
                }

                if(e.stopPropagation) {
                    e.stopPropagation();
                }

                e.dataTransfer.dropEffect = 'move';
                return false;
            });

            el.bind("dragenter", function(e) {
                angular.element(e.target).addClass('drag-over');
            });

            el.bind("dragleave", function(e) {
                angular.element(e.target).removeClass('drag-over');  // this / e.target is previous target element.
            });

            el.bind("drop", function(e) {
                if (e.preventDefault) {
                    e.preventDefault(); // Necessary. Allows us to drop.
                }

                if (e.stopPropogation) {
                    e.stopPropogation(); // Necessary. Allows us to drop.
                }

                var data = e.dataTransfer.getData("text");
                var dest = document.getElementById(id);
                var src = document.getElementById(data);
                $scope.ngOnDrop(src, dest);
            });

            $rootScope.$on("DRAG-START", function(e) {
                var el = document.getElementById(id);
                angular.element(el).addClass("drop-target");
                angular.element(el).addClass('drag-in-progress');
            });

            $rootScope.$on("DRAG-END", function(e) {
                var el = document.getElementById(id);
                angular.element(el).removeClass("drop-target");
                angular.element(el).removeClass("drag-over");
                angular.element(el).removeClass('drag-in-progress');
            });
        }
    }
}]);