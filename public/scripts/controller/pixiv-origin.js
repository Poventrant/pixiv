
'use strict';

angular.module("mainApp").controller('pixivOriginCtrl',
	function($scope, $http, $routeParams, PixivResource) {
        var authorId = $routeParams.authorId,
            picid = $routeParams.picid,
            type = $routeParams.type;
        const picno = parseInt( $routeParams.picno );

        $scope.pixiv = {};
        $scope.pixiv.authorId = authorId;
        $scope.pixiv.picid = picid;
        $scope.pixiv.type = type;
        $scope.pixiv.picno = new Array(picno);
	})