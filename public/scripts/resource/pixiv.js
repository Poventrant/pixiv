/**
 *
 * Created by Kaze on 2/10/14.
 */
'use strict';
angular.module('mainApp')
	.factory('PixivResource', function(baseResource) {
		var resourceType = 'pixiv';
		return baseResource(resourceType, {
			queryByPage: {
				method: 'GET',
				url: resourceType + '/queryByPage'
			}
		});
	}).factory("PixivPageUtil", function() {
		return function(pm) {
			var pages = [];
			if (pm.hasPrePage) {
				var temp = {};
				temp['text'] = '首页';
				temp['target'] = 1;
				pages.push(temp);
				temp = {};
				temp['text'] = '上一页';
				temp['target'] = pm.currentPage - 1;
				pages.push(temp);
			}
			for (var i = pm.begin; i <= pm.end; ++i) {
				var temp = {};
				temp['text'] = i;
				temp['target'] = i;
				if (i == pm.currentPage) {
					temp['active'] = 'active';
				}
				pages.push(temp);
			}
			if (pm.hasNextPage) {
				var temp = {};
				temp['text'] = '下一页';
				temp['target'] = pm.currentPage + 1;
				pages.push(temp);
				temp = {};
				temp['text'] = '尾页';
				temp['target'] = pm.totalPage;
				pages.push(temp);
			}
			return pages;
		}
	}).factory("PixivUrlUtil", function($location) {
		return function(param) {
			var base = "/#/pixiv";
			var handler = function(value, key, baseUrl) {
				if (baseUrl == '') {
					return "?" + key + "=" + value;
				}
				var reg = "([/?]|&)" + key + "=([^&]*)(&|$)";
				var res = baseUrl.match(reg);
				if (res == null) {
					return baseUrl + "&" + key + "=" + value;
				} else {
					var tempReg = new RegExp(key + "=([^&]*)");
					return baseUrl.replace(tempReg, key + "=" + value);
				}
			}
			if(Object.prototype.toString.apply(param) === "[object Array]") {
			   	var temp = $location.$$url.substr(6);
				for(var i = 0; i < param.length; ++ i) {
					if(Object.prototype.toString.apply(param[i]) !== "[object Object]") {
						return null;
					}
					temp = handler(param[i].value, param[i].text, temp);
				}
				return base + temp;
			} else if(Object.prototype.toString.apply(param) === "[object Object]"){
				return  base + handler(param.value, param.text, $location.$$url.substr(6));
			} 
			return null;
		}
	});