
'use strict';

angular.module("mainApp").controller('pixivAddCtrl',
	function($scope, $http, UtilService, PixivResource) {
		var errHandler = function() {
			UtilService.alertError('提示', '链接失败');
		};
		var sucHandler = function() {
			UtilService.alertSuccess('提示', '下载添加成功');
		};

		PixivResource.getAuthors({}).$promise.then(function(res) {
			$scope.authors = res.authors;
			$scope.author = $scope.authors[0];
		});

		$scope.doAdd = function() {
			PixivResource.put({
				authorId: $scope.authorId
			}).$promise.then(function(res) {
				UtilService.alert('提示', res.msg);
			})
		};

		$scope.doDelete = function() {
			if(!confirm("你确定要删除吗？")) return false;
			PixivResource.deleteByAuthor({
				author: $scope.author
			}).$promise.then(function(res) {
				if(res.success) {
					window.location.reload();
				} else {
					UtilService.alert('提示', res.msg);
				}
			})
		};

		$scope.doAllDelete = function() {
			if(!confirm("你确定要全部删除吗？")) return false;
			PixivResource.deleteAll({
			}).$promise.then(function(res) {
				if(res.success) {
					window.location.reload();
				} else {
					UtilService.alert('提示', res.msg);
				}
			})
		};

		$scope.resetPixivLoginCookie = function() {
			$scope.overLay = true;
		};

		$scope.closeOverLay = function() {
			$scope.overLay = false;
		};

		$scope.pixiv = {};
		$scope.setPixivCookie = function() {
			PixivResource.setPixivCookie($scope.pixiv)
			.$promise.then(function(res) {
				if(res.success) {
					$scope.overLay = false;
					UtilService.alert('提示', res.msg);
				} else {
					UtilService.alert('提示', res.msg);
				}
			})
		}

	});