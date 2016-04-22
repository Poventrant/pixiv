
'use strict';

angular.module("mainApp").filter("mutipleFilter", function() {
    return function(str) {
        var strs = str.split("/");
        var len = strs.length;
        var groups = /([^_]*)[^.]*[\.](.*)/.exec(strs[len-1]);
        return strs[len-2] + "/" + groups[1] + "/" + groups[2];
    }
});
