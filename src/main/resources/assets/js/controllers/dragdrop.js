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
            ghostElement: '@',
            dragData: '=',
            ngDraggable: '='
        },
        link: function(scope, el, attrs, controller) {
            function dragStartFunc(e) {
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
            }

            function dragEndFunc(e) {
                $rootScope.$emit("DRAG-END");
            }

            var id = angular.element(el).attr("id");

            if (!id) {
                id = guid()
                angular.element(el).attr("id", id);
            }

            scope.$watch('ngDraggable', function(value) {
                if (value) {
                    angular.element(el).attr("draggable", "true");
                    if (scope.dragData) {
                        $.data(el.get(0), 'drag-drop-data', scope.dragData);
                    }
                    el.bind("dragstart", dragStartFunc);
                    el.bind("dragend", dragEndFunc);
                }
                else {
                    el.removeAttr("draggable");
                    el.unbind("dragstart", dragStartFunc);
                    el.unbind("dragend", dragEndFunc);
                }
            });

        }
    }
}]);

App.directive('ngDroppable', ['$rootScope', function($rootScope) {
    return {
        restrict: 'A',
        scope: {
            ngOnDrop: '&',
            ngDroppable: '='
        },
        link: function($scope, el, attrs, controller) {
            function dragoverFunc(e) {
                if (e.preventDefault) {
                    e.preventDefault(); // Necessary. Allows us to drop.
                }

                if(e.stopPropagation) {
                    e.stopPropagation();
                }

                e.dataTransfer.dropEffect = 'move';
                return false;
            }
            function dragenterFunc(e) {
                angular.element(e.target).addClass('drag-over');
            }
            function dragleaveFunc(e) {
                angular.element(e.target).removeClass('drag-over');  // this / e.target is previous target element.
            }
            function dropFunc(e) {
                if (e.preventDefault) {
                    e.preventDefault(); // Necessary. Allows us to drop.
                }

                if (e.stopPropogation) {
                    e.stopPropogation(); // Necessary. Allows us to drop.
                }

                var data = e.dataTransfer.getData("text");
                var dest = document.getElementById(id);
                var src = document.getElementById(data);
                $scope.ngOnDrop({
                    dragEl: src,
                    dropEl: dest,
                    data: $.data(src, 'drag-drop-data')
                });
            }
            var dragStartOff;
            var dragEndOff;


            var id = angular.element(el).attr("id");
            if (!id) {
                id = guid()
                angular.element(el).attr("id", id);
            }

            $scope.$watch('ngDroppable', function(value) {
                if (value) {
                    el.bind("dragover", dragoverFunc);
                    el.bind("dragenter", dragenterFunc);
                    el.bind("dragleave", dragleaveFunc);
                    el.bind("drop", dropFunc);
                    dragStartOff = $rootScope.$on("DRAG-START", function(e) {
                        var el = document.getElementById(id);
                        angular.element(el).addClass("drop-target");
                        angular.element(el).addClass('drag-in-progress');
                    });
                    dragEndOff = $rootScope.$on("DRAG-END", function(e) {
                        var el = document.getElementById(id);
                        angular.element(el).removeClass("drop-target");
                        angular.element(el).removeClass("drag-over");
                        angular.element(el).removeClass('drag-in-progress');
                    });
                }
                else {
                    el.unbind("dragover", dragoverFunc);
                    el.unbind("dragenter", dragenterFunc);
                    el.unbind("dragleave", dragleaveFunc);
                    el.unbind("drop", dropFunc);
                    if (dragStartOff) { dragStartOff(); }
                    if (dragEndOff) { dragEndOff(); }
                }
            });

        }
    }
}]);