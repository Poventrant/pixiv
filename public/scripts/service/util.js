'use strict';

angular.module('mainApp')
    .service('UtilService', function () {
        var containerId = 'util-alert-messages',
            $container = $('#' + containerId),
            alertMsg,
            confirmWin;
        if (!$container.length) {
            $container = $('<ul>').addClass('util-alert-messages').attr('id', containerId).appendTo(document.body);
        }
        // 提示信息
        alertMsg = function (title, content, cls) {
            if(content){
                var $msg = $('<li>')
                    .hide()
                    .addClass(cls || 'lazur-bg')
                    .append($('<div>').addClass('util-alert-message-title').html(title))
                    .append($('<div>').html(content))
                    .prependTo($container)
                    .fadeIn();
                setTimeout(function() {
                    $msg.fadeOut(function () {
                        $msg.remove();
                    });
                }, 5000);
            }
        };
        // 确认弹窗
        confirmWin = function (msg, successHandler, cancelHandler) {
            var cf = window.confirm(msg);
            if (cf) {
                if (angular.isFunction(successHandler)) {
                    successHandler();
                }
            } else if (angular.isFunction(cancelHandler)) {
                cancelHandler();
            }
        };
        //验证form中文本框输入是否包含特殊字符
        var validateInputValue = function(form) {
            var result = true
            var pattern = new RegExp("[%--`~!@#$^&*()\\[\\].<>/?~！@#￥……&*]");
            var eform = form[0];
            var size = form[0].length;
            for(var i=0;i<size;i++){
                if(eform[i].type=="text"){
                    if((eform[i].value).match(pattern)) {
                        result = false;
                        return;
                    }
                }
            }
            return result;
        };
        return {
            alert: alertMsg,
            // 成功提示
            alertSuccess: function (title, content) {
                alertMsg(title, content, 'navy-bg');
            },
            // 错误提示
            alertError: function (title, content) {
                alertMsg(title, content, 'danger-bg');
            },
            confirm: confirmWin,
            validateTextInput: function(form) {
                validateInputValue(form);
            }
        };
    })
    .service('CalculateService',function(){
        //加法函数
        var accAdd = function(arg1, arg2) {
            var r1, r2, m;
            try {
                r1 = arg1.toString().split(".")[1].length;
            } catch (e) {
                r1 = 0;
            }
            try {
                r2 = arg2.toString().split(".")[1].length;
            } catch (e) {
                r2 = 0;
            }
            m = Math.pow(10, Math.max(r1, r2));
            return (arg1 * m + arg2 * m) / m;
        }
        //减法函数
        var accSub = function(arg1, arg2) {
            var r1, r2, m, n;
            try {
                r1 = arg1.toString().split(".")[1].length;
            }catch (e) {
                r1 = 0;
            }
            try {
                r2 = arg2.toString().split(".")[1].length;
            }
            catch (e) {
                r2 = 0;
            }
            m = Math.pow(10, Math.max(r1, r2));
            //动态控制精度长度
            n = (r1 >= r2) ? r1 : r2;
            return ((arg1 * m - arg2 * m) / m).toFixed(n);
        }

        //乘法函数
        var accMul = function(arg1, arg2) {
            var m = 0, s1 = arg1.toString(), s2 = arg2.toString();
            try {
                m += s1.split(".")[1].length;
            }
            catch (e) {
            }
            try {
                m += s2.split(".")[1].length;
            }
            catch (e) {
            }
            return Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m);
        }

        //除法函数
        var accDiv = function (arg1, arg2) {
            var t1 = 0, t2 = 0, r1, r2;
            try {
                t1 = arg1.toString().split(".")[1].length;
            }
            catch (e) {
            }
            try {
                t2 = arg2.toString().split(".")[1].length;
            }
            catch (e) {
            }
            r1 = Number(arg1.toString().replace(".", ""));
            r2 = Number(arg2.toString().replace(".", ""));
            return (r1 / r2) * Math.pow(10, t2 - t1);
        }
        return{
            add : accAdd,
            sub : accSub,
            mul : accMul,
            div : accDiv
        };
    })
    .service('MoneyInWordsService',function(){
        var numToCny = function(num) {
            // /<summery>小写金额转化大写金额</summery>
            // /<param name=num type=number>金额</param>
            if (isNaN(num))
                return "无效数值！";
            var strPrefix = "人民币";
            if (num < 0)
                strPrefix = "(负)人民币";
            num = Math.abs(num);
            if (num >= 1000000000000)
                return "无效数值！";
            var strOutput = "";
            var strUnit = '仟佰拾亿仟佰拾万仟佰拾元角分';
            var strCapDgt = '零壹贰叁肆伍陆柒捌玖';
            num += "00";
            var intPos = num.indexOf('.');
            if (intPos >= 0) {
                num = num.substring(0, intPos)
                    + num.substr(intPos + 1, 2);
            }
            strUnit = strUnit.substr(strUnit.length - num.length);
            for (var i = 0; i < num.length; i++) {
                strOutput += strCapDgt.substr(num.substr(i, 1), 1)
                    + strUnit.substr(i, 1);
            }
            return strPrefix
                + strOutput.replace(/零角零分$/, '整').replace(
                    /零[仟佰拾]/g, '零').replace(/零{2,}/g, '零')
                    .replace(/零([亿|万])/g, '$1').replace(
                        /零+元/, '元').replace(/亿零{0,3}万/,
                        '亿').replace(/^元/, "零元");
        }
        return{
            numToCny : numToCny
        }
    })
    .service('FileService', function() {
        var maxsize = 5*1024*1024;// 5M
        var checkfile = function(obj_file){
            try{
                if(obj_file.value==""){
                    return false;
                }
                var filesize = obj_file.size;

                if(filesize>maxsize){
                    return false;
                }
                return true;
            }catch(e){
                console.log(e);
            }
        }

        return{
            checkfile : checkfile
        }
    })
    .service('DateTimeService',function(){
        var getNowFormatDate = function(){
            var date = new Date();
            var seperator1 = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            var currentdate = year + seperator1 + month + seperator1 + strDate;
            return currentdate;
        }
        return{
            getNowFormatDate : getNowFormatDate
        }
    })
    // postMessage 相关服务
    .service('PostMessageService', function () {
        var handlers = {};
        window.addEventListener('message', function (e) {
            var msg = e.message || e.data || '',
                idx = msg.indexOf(':'),
                type;
            if (idx > 0) {
                type = msg.substr(0, idx);
                if (angular.isFunction(handlers[type])) {
                    handlers[type](JSON.parse(msg.substr(idx + 1)));
                }
            }
        }, false);
        return {
            listen: function (type, handler) {
                handlers[type] = handler;
            },
            trigger: function (type, data) {
                var iframes = document.getElementsByTagName('iframe');
                if (iframes.length) {
                    iframes[0].contentWindow.postMessage('editor-' + type + ':' + JSON.stringify(data || {}), '*');
                }
            }
        };
    });

