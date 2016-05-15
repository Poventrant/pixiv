'use strict';

angular.module('mainApp')
    /*
     * 将 $http 方法的 data 按 form 方式发送
     */
    .factory('transformRequestAsFormPostUtil', function () {
        function serializeData(data) {
            // If this is not an object, defer to native stringification.
            if (!angular.isObject(data)) {
                return data ? data.toString() : '';
            }

            var buffer = [],
                name,
                val;
            // Serialize each key in the object.
            for (name in data) {
                if (data.hasOwnProperty(name)) {
                    val = data[name];
                    buffer.push(
                        encodeURIComponent(name) +
                        '=' +
                        encodeURIComponent(val === null ? '' : val)
                    );
                }
            }
            // Serialize the buffer and clean it up for transportation.
            return buffer.join('&').replace(/%20/g, '+');
        }

        // I prepare the request data for the form post.
        return function transformRequest(data, getHeaders) {
            var headers = getHeaders();
            headers['Content-Type'] = 'application/x-www-form-urlencoded';
            return serializeData(data);
        };
    })
    /*
     * 检查登录状态
     */
    .factory('transformResponseCheckLoginUtil', function (UtilService) {
        return function (res) {
            var data;
            try {
                data = JSON.parse(res);
                // 检查 data.code
                if (data.code) {
                    switch (data.code) {
                        // 没登录
                        case 101:
                            location.href = 'login';
                            break;
                        // 没有权限
                        default:
                            UtilService.alertError('提示',data.msg);
                        //window.alert(data.msg);
                    }
                }
            } catch (e) {
            }
            // 原数据返回
            return data;
        };
    })
    /*
     * 统一全部 RESTful 资源加载方式
     */
    .factory('BaseResource', function ($resource, transformRequestAsFormPostUtil, transformResponseCheckLoginUtil) {
        return function (resourceType, extActions) {
            // 重写 action
            var actions = {
                // 查询列表
                query: {
                    method: 'GET',
                    url: resourceType + '/queryAll',
                    isArray: true
                    // cache: true
                },
                querys: {
                    method: 'GET',
                    url: resourceType + '/querys',
                    //isArray: true
                    // cache: true
                },
                get: {
                    method: 'GET',
                    url: resourceType + '/find'
                },
                // 添加
                put: {
                    method: 'POST',
                    url: resourceType + '/create'
                },
                // 更新
                save: {
                    method: 'POST',
                    url: resourceType + '/update'
                },
                // 删除
                remove: {
                    method: 'POST',
                    url: resourceType + '/delete'
                },
                //作废
                cancel: {
                    method: 'POST',
                    url: resourceType + '/cancel'
                },
                checkUnique: {
                    method: 'GET',
                    url: resourceType + '/checkUnique'
                },
            };
            // 扩展 action
            if (extActions) {
                angular.extend(actions, extActions);
            }
            angular.forEach(actions, function (action) {
                action.transformRequest = transformRequestAsFormPostUtil;
                //去掉判断，系统错误信息都做提示	廖金洪
//                if (action.method === 'POST') {
                action.transformResponse = transformResponseCheckLoginUtil;
//                }
            });

//            alert(resourceType + '/:id');
            return $resource(resourceType + '/:id', null, actions);
        };
    })
    /*
     * form 方式发送的 post 方法
     */
    .factory('formPostUtil', function ($http, transformRequestAsFormPostUtil, transformResponseCheckLoginUtil) {
        return function (path, data) {
            return $http({
                url: path,
                method: 'POST',
                transformRequest: transformRequestAsFormPostUtil,
                transformResponse: transformResponseCheckLoginUtil,
                data: data
            });
        };
    })
    //定义全局bootstrap参数
    .factory('bsTable',function(UtilService){
        var queryParams = function(params) {
            return {
                pageSize : params.limit,
                pageNumber : params.offset,
                sortName : params.sort,
                sortOrder : params.order,
                searchText : params.search
            };
        };
        var bs_table = {
            height : $(document).height()-200,
            method : 'post',
            dataType : "json",
            contentType : "application/x-www-form-urlencoded",
            sidePagination : "server",
            pagination : true,
            cache : false,
            striped : true,
            pageSize : 10,
            pageList : [ 10, 25, 50, 100, 200 ],
            search : true,
            showColumns : true,
            showRefresh : true,
            minimumCountColumns : 2,
            clickToSelect : true,
            defaultVisible : [],
//			queryParamsType : "no_limit",
            queryParams : queryParams,
            paginationFirstText : "首页",
            paginationPreText : "上一页",
            paginationNextText : "下一页",
            paginationLastText : "尾页",
            responseHandler: function (res) {
                if(!res.success){
                    var msg = res.msg? res.msg : "操作出错";
                    UtilService.alertError('提示',msg);
                }
                return res;
            }
        };
        return bs_table;
    })
    .factory('postFormResource',function($resource) {
        return function (resourceType, extActions) {
            var actions = {
                // 查询列表
                create: {
                    method: 'POST',
                    url: resourceType + '/create'
                }

            };
            return $resource(resourceType,null, actions);
        }
    });