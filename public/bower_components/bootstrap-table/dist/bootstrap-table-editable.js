/**
 * @author zhixin wen <wenzhixin2010@gmail.com>
 * extensions: https://github.com/vitalets/x-editable
 */

!function ($) {

    'use strict';

    $.extend($.fn.bootstrapTable.defaults, {
        editable: false,
        checkEditable:'',
        editFunction:'',
        onEditableInit: function () {
            return false;
        },
        onEditableSave: function (field, row, oldValue, $el) {
            return false;
        },
        onEditableShown: function (field, row, $el, editable) {
            return false;
        },
        onEditableHidden: function (field, row, $el) {
            return false;
        }
    });

    $.extend($.fn.bootstrapTable.Constructor.EVENTS, {
        'editable-init.bs.table': 'onEditableInit',
        'editable-save.bs.table': 'onEditableSave',
        'editable-shown.bs.table': 'onEditableShown',
        'editable-hidden.bs.table': 'onEditableHidden'
    });
    
    $.extend($.fn.bootstrapTable.columnDefaults, {
        nullable: false,
    });

    var BootstrapTable = $.fn.bootstrapTable.Constructor,
        _initTable = BootstrapTable.prototype.initTable,
        _initBody = BootstrapTable.prototype.initBody;

    BootstrapTable.prototype.initTable = function () {
        var that = this;
        _initTable.apply(this, Array.prototype.slice.apply(arguments));

        if (!this.options.editable) {
            return;
        }

        $.each(this.options.columns, function (i, column) {
        	
            if (!column.editable) {
                return;
            }

            var _formatter = column.formatter;
            
            column.formatter = function (value, row, index) {
                var result = _formatter ? _formatter(value, row, index) : value;
                // 查看当前row是否可以编辑
                var func = that.options.checkEditable;
                var editable = true;
                $.each(that.options,function(i,option){
                	if(typeof option == 'function' && i==func){
                		editable = option.apply(that,[row]);
                	}
                });
                
                var type = "text";
                
                // 格式化当前column的值
                if(column.gcode && that.options.sysparam){
                	var gcodes = that.options.sysparam[column.gcode];
                	var array =[];
                    for (var key in gcodes){
                    	array.push({id:gcodes[key].mcode,text:gcodes[key].mname});
                    	if(value == gcodes[key].mcode)
                    		result = gcodes[key].mname;
                    };
                    type = "select2";
                    column.value=array;
            	}
                
                // 不能编辑
                if(!editable){
                	return result ? result : '-';
                }
                // 可以编辑
                var formatter = 
                	['<a href="javascript:void(0)"',
                	 ' data-name="' + column.field + '"',
                	 ' data-pk="' + row[that.options.idField] + '"',
                	 ];
                
                
                if(column.type=="date"){
                	type="combodate";
                }
                
                result = !result ? "" : result;
                
                formatter.push('data-type="'+type+'"');
                formatter.push( '>' +result+ '</a>');
                    
                return formatter.join('');
            };
            
        });
    };

    BootstrapTable.prototype.initBody = function () {
        var that = this;
        _initBody.apply(this, Array.prototype.slice.apply(arguments));

        if (!this.options.editable) {
            return;
        }

        $.each(this.options.columns, function (i, column) {
            if (!column.editable) {
                return;
            }
            var e = that.$body.find('a[data-name="' + column.field + '"]');
            that.$body.find('a[data-name="' + column.field + '"]').editable({source:column.value})
                .off('save').on('save', function (e, params) {
                    var allData = that.getData(),
                    	
                        index = $(this).parents('tr[data-index]').data('index'),
                        row = allData[index],
                        oldValue = row[column.field];
                    	var newRow = new Object();
                    	$.extend(newRow,row);
                    	newRow[column.field] = params.submitValue;
                    
                    if(!column.nullable && !params.submitValue){
                    	alert('选项不能为空');
                    	params.newValue = oldValue;
                    	return ;
                    }
                    
                    if(!that.options.saveUrl){
                    	var func = that.options.editFunction;
                        $.each(that.options,function(i,option){
                        	if(typeof option == 'function' && i==func){
                        		option.apply(that,[index,newRow,column]);
                        	}
                        });
                    	return;
                    }
                    
                    $.ajax({
                        url     : that.options.saveUrl,
                        data    : JSON.stringify(newRow),
                        dataType: 'json',
                        type	: 'POST',
                        async: false,
                        success: function(data){ 
                        	if (data.success) {
                        		var updateData = data.data;
                        		if(updateData){
                        			$.each(updateData,function(key,val){
                        				row[key] = val;
                                    });
                        		}
                        		row[column.field] = params.submitValue;
                        		that.$el.bootstrapTable('updateRow',{index:index,row:row});
                        		return ;
            				}else if (data.msg) {
            					alert(data.msg);
            				}else{
            					alert('保存失败');
            				}
                        	params.newValue = oldValue;
        					return ;
                        },
                        error: function(){  
                        	alert('保存失败');
                        	params.newValue = oldValue;
                        	return ;
                        }
                    });
                    
                });
            that.$body.find('a[data-name="' + column.field + '"]').editable({source:column.value})
                .off('shown').on('shown', function (e) {
                    var data = that.getData(),
                        index = $(this).parents('tr[data-index]').data('index'),
                        row = data[index];
                    
                    that.trigger('editable-shown', column.field, row, $(this));
                });
            that.$body.find('a[data-name="' + column.field + '"]').editable({source:column.value})
                .off('hidden').on('hidden', function (e, editable) {
                    var data = that.getData(),
                        index = $(this).parents('tr[data-index]').data('index'),
                        row = data[index];
                    
                    that.trigger('editable-hidden', column.field, row, $(this), editable);
                });
        });
        this.trigger('editable-init');
    };

   /* $.extend($.fn.editable.defaults, {
    	update:false,
        save: function (field, text) {
            return false;
        }
        
    });
    
    var Editable = $.fn.Editable.Constructor;*/
    
}(jQuery);
