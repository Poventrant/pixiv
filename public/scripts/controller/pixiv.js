'use strict';

angular.module("mainApp").controller('pixivCtrl',
	function($scope, $http, $routeParams, UtilService, PixivResource, PixivPageUtil, PixivUrlUtil) {
		var errHandler = function() {
			UtilService.alertError('提示', '链接失败');
		};
		var sucHandler = function() {
			UtilService.alertSuccess('提示', '下载添加成功');
		};
		$scope.sorts = [{
			value: '0',
			text: '默认'
		}, {
			value: '1',
			text: '分数'
		}, {
			value: '2',
			text: '大小'
		}, {
			value: '3',
			text: '创建时间'
		}];

		$scope.querys = {};

		if ($routeParams.page) {
			$scope.querys.page = $routeParams.page
		} else {
			$scope.querys.page = 0;
		}
		if ($routeParams.mod) {
			$scope.querys.mod = $routeParams.mod;
		} else {
			$scope.querys.mod = "authorType";
		}
		if ($routeParams.author) {
			$scope.querys.author = $routeParams.author;
		} else {
			$scope.querys.author = '默认';
		}
		if ($routeParams.sort) {
			$scope.querys.sort = $routeParams.sort;
		} else {
			$scope.querys.sort = '0';
		}

		PixivResource.queryByPage({
			querysModel: $scope.querys,
		}).$promise.then(function(res) {
			if(res.success) {
				$scope.pixivList = res.pixivList;

				$scope.authors = res.authors;

				$scope.pages = PixivPageUtil(res.pageModel);

				if ($routeParams.author) {
					$scope.querys.author = $routeParams.author;
				} else {
					$scope.querys.author = $scope.authors[0];
				}
			} else {
				UtilService.alert('提示', res.msg);
			}
		});


		$scope.toNew = function(target) {
			var link = PixivUrlUtil({
				value:target,
				text: "page"
			});
			location.href = link;
		};

		$scope.toSelect = function(target) {
			var link = PixivUrlUtil([{
				value: encodeURIComponent( $scope.querys.author ),
				text: "author"
			}, {
				value: $scope.querys.mod,
				text: "mod"
			}, {
				value: $scope.querys.sort,
				text: "sort"
			}, {
				value:1,
				text: "page"
			}]);
			location.href = link;
		}
	});