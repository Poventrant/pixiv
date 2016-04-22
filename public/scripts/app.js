'use strict';

angular.module('mainApp', ['ngRoute','ngResource'])
    .config(function($routeProvider,$httpProvider) {

        // 路由配置,
        $routeProvider
            .when('/pixiv', {
                templateUrl: 'views/pixiv.html',
                controller : 'pixivCtrl'
            })
            .when('/pixiv/add', {
                templateUrl: 'views/pixiv-add.html',
                controller : 'pixivAddCtrl'
            })
            .when('/pixiv/origin/:authorId/:picid/:type/:picno', {
                templateUrl: 'views/pixiv-origin.html',
                controller : 'pixivOriginCtrl'
            })
            .otherwise({
                templateUrl: 'views/404.html'
            });

    });