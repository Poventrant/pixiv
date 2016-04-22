/**
 * @author: aperez <aperez@datadec.es>
 * @version: v2.0.0
 *
 * @update Dennis Hernández <http://djhvscf.github.io/Blog>
 */

!function($) {
    'use strict';

    var firstLoad = false;

    var sprintf = $.fn.bootstrapTable.sprintf;
    
    var calculateObjectValue = $.fn.bootstrapTable.calculateObjectValue;

    var showAvdSearch = function(pColumns, searchTitle, searchText, that) {
        if (!$("#avdSearchModal" + "_" + that.options.idTable).hasClass("modal")) {
            var vModal = sprintf("<div id=\"avdSearchModal%s\" class=\"modal fade in\" tabindex=\"1\" role=\"dialog\" style=\"z-index:1200\" aria-labelledby=\"mySmallModalLabel\" aria-hidden=\"true\">", "_" + that.options.idTable);
            vModal += "<div class=\"modal-dialog modal-lg\">";
            vModal += " <div class=\"modal-content\">";
            vModal += "  <div class=\"modal-header\">";
            vModal += "   <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\" >&times;</button>";
            vModal += sprintf("   <h4 class=\"modal-title\">%s</h4>", searchTitle);
            vModal += "  </div>";
            vModal += "  <div class=\"modal-body modal-body-custom\" style=\"max-height:600px\">";
            vModal += sprintf("   <div class=\"container-fluid\" id=\"avdSearchModalContent%s\" style=\"padding-right: 0px;padding-left: 0px;\" >", "_" + that.options.idTable);
            vModal += "   </div>";
            vModal += "  </div>";
            vModal += "  </div>";
            vModal += " </div>";
            vModal += "</div>";

            $("body").append($(vModal));
            var vFormAvd = createFormAvd(pColumns, searchText, that);
             
            $('#avdSearchModalContent' + "_" + that.options.idTable).append(vFormAvd.join(''));

            $('.daterange').daterangepicker({autoApply:true,singleDatePicker:false,locale:{"format": "YYYY/MM/DD","separator":"~"}});
            $('.selectpicker').selectpicker({size: 4});
            
            $("#btnReset" + "_" + that.options.idTable).click(function(event) {
            	var form = $('#'+that.options.idForm);
            	form.children().children().children().each(function(){
            		var tag = $(this).prop('tagName');
            		if(tag=='INPUT'){
            			$(this).val('');
            		}
            	});
            	
                $('.selectpicker').selectpicker('val','');
                that.options.moreConditions=[];
                that.options.pageNumber = 1;
            	that.onSearch(event);
            	that.updatePagination();
            });
            
            
            $("#btnSearch" + "_" + that.options.idTable).click(function(event) {
            	var form = $('#'+that.options.idForm);
            	var conditions = [];
            	that.options.moreConditions=[];
            	// 输入框，日期选择框,单选框和多选框
            	form.children().children().children().each(function(){

            		var tag = $(this).prop('tagName');
            		
            		if(tag=='INPUT' || tag=='SELECT'){
            			
                		var id =  $(this).attr('id');
                		var value =  $(this).val();
                		
            			if($.isArray(value)){
            				value=value.join(",");
	            		}
	            		if(value){
	            			conditions[id]=value;
	            		}
            		}
            	});
            	/*form.find('input').each(function () {
            		var id =  $(this).attr('id');
            		var value =  $(this).val();
            		if(value){
            			conditions[id]=value;
            		}
            	});
            	// 单选框和多选框
            	form.find('select').each(function () {
            		var id =  $(this).attr('id');
            		var value =  $(this).val();
            		if($.isArray(value)){
            			value=value.join(",");
            		}
            		if(value){
            			conditions[id]=value;
            		}
            	});*/
            	
            	$.extend(that.options.moreConditions,conditions);
            	that.options.pageNumber = 1;
            	that.onSearch(event);
            	that.updatePagination();
            });

            $("#avdSearchModal" + "_" + that.options.idTable).modal('show');
        } else {
            $("#avdSearchModal" + "_" + that.options.idTable).modal('show');
        }
        
    };

    var createFormAvd = function(pColumns, searchText, that) {
    	var bootstrapTable = $.fn.bootstrapTable;
        var htmlForm = [];
        htmlForm.push("<iframe name=\"currentPage\" style=\"display:none;\"></iframe>"); 
        htmlForm.push(sprintf('<div class="form-horizontal" id="%s" target="currentPage" >', that.options.idForm, that.options.actionForm));
        var count=0;
        for (var i in pColumns) {
            var vObjCol = pColumns[i];
            if (!vObjCol.checkbox && vObjCol.searchable && vObjCol.searchCondition) {
            	if(count==0 || count%2==0){
            		htmlForm.push('<div class="form-group">');
            	}
            	htmlForm.push(sprintf('<label class="col-sm-2 control-label">%s</label>', vObjCol.title));
            	htmlForm.push('<div class="col-sm-4">');
            	if(vObjCol.gcode && bootstrapTable.defaults.sysparam && vObjCol.type){
            		var filterStr ='',
            			filter = [],
            			userInfo = bootstrapTable.defaults.userInfo,
				  		gcodes = bootstrapTable.defaults.sysparam[vObjCol.gcode];
				  	
				  	switch(vObjCol.gcode)
				  	{
					  	case 'INCOME_CODE':
					  		filterStr=userInfo.incomeType;
					  	  break;
					  	case 'AR_COMPANY':
					  		filterStr=userInfo.companyPrivs;
					  	  break;
					  	case 'BUDGET_DEPT':
					  		filterStr=userInfo.budgetPrivs;
					  	break;
					  	case 'BENEFIT_DEPT':
					  		filterStr=userInfo.benefitPrivs;
					  	break;
				  	}
				  	
				  	if(!$.isEmptyObject(filterStr)){
				  		filter=filterStr.split(",");
				  	}
				  	
				  	if(vObjCol.type=="multi"){
				  		htmlForm.push(sprintf('<select class="selectpicker" multiple data-hidden="%s" data-live-search="true" data-selected-text-format="count" id="%s" title="==请选择==">',vObjCol.hidden,vObjCol.field));
				  	}else{
				  		htmlForm.push(sprintf('<select class="selectpicker" data-hidden="%s" data-live-search="true" id="%s">',vObjCol.hidden,vObjCol.field));
				  		htmlForm.push(sprintf('<option value="">==请选择==</option>'));
				  	}
				  	for (var key in gcodes){
				  		if($.isEmptyObject(filter)|| $.inArray(gcodes[key].mcode, filter)!=-1){
				  			htmlForm.push(sprintf('<option value="%s">%s</option>',gcodes[key].mcode,gcodes[key].mname));
				  		}
				  	}
				  	htmlForm.push('</select>');
				  	
            	}else if(vObjCol.type=="date"){
            		htmlForm.push(sprintf('<input class="form-control input-md daterange" type="text" name="%s" id="%s"/>', vObjCol.field,vObjCol.field));
            		
            	}else{
                    htmlForm.push(sprintf('<input type="text" class="form-control input-md" name="%s" placeholder="%s" id="%s" >', vObjCol.field, vObjCol.placeholder?vObjCol.placeholder:vObjCol.title, vObjCol.field));
            	}
            	
            	htmlForm.push('</div>');
            	if((count+1)%2==0){
            		htmlForm.push('</div>');
            	}
            	count++;
            }
        }

        htmlForm.push('<div class="form-group">');
        htmlForm.push('<div class="col-sm-offset-8 col-sm-4" style="padding-top: 80px;"> ');
        htmlForm.push(sprintf('<button role="button" id="btnReset%s" class="btn btn-default col-sm-offset-4" >重置</button>', "_" + that.options.idTable));
        htmlForm.push(sprintf('<button role="button" id="btnSearch%s" class="btn btn-default col-sm-offset-2" >查询</button>', "_" + that.options.idTable));
        htmlForm.push('</div>');
        htmlForm.push('</div>');
        htmlForm.push('</div>');
        return htmlForm;
    };

    $.extend($.fn.bootstrapTable.defaults, {
    	userInfo:undefined,
    	type:'',
    	placeholder:'',
        idForm: 'advancedSearch',
        actionForm: '',
        idTable: undefined,
        saveUrl:'',
        onColumnAdvancedSearch: function (field, text) {
            return false;
        }
    });

    $.extend($.fn.bootstrapTable.defaults.icons, {
        advancedSearchIcon: 'glyphicon glyphicon-search'
    });

    $.extend($.fn.bootstrapTable.Constructor.EVENTS, {
        'column-advanced-search.bs.table': 'onColumnAdvancedSearch'
    });

    $.extend($.fn.bootstrapTable.locales, {
        formatAdvancedSearch: function() {
            return '高级查询';
        },
        formatAdvancedCloseButton: function() {
            return "关闭";
        }
    });
    
    $.extend($.fn.bootstrapTable.columnDefaults, {
    	searchCondition:false,	//是否作为搜索条件 黄玉云
    	hidden:false //下拉菜单是否默认显示选项
    });

    $.extend($.fn.bootstrapTable.defaults, $.fn.bootstrapTable.locales);

    var BootstrapTable = $.fn.bootstrapTable.Constructor,
        _initToolbar = BootstrapTable.prototype.initToolbar,        
        _load = BootstrapTable.prototype.load,
        _initSearch = BootstrapTable.prototype.initSearch;

    BootstrapTable.prototype.initToolbar = function() {
        _initToolbar.apply(this, Array.prototype.slice.apply(arguments));

        if (!this.options.advancedSearch) {
            return;
        }

        if (!this.options.idTable) {
            return;
        }

        var that = this,
            html = [],
        	$btnGroup = that.$toolbar.find('>.btn-group'),
        	$search = $btnGroup.find('div.advancedSearch');

        //html.push(sprintf('<div class="columns columns-%s btn-group pull-%s" role="group">', this.options.buttonsAlign, this.options.buttonsAlign));
        html.push(sprintf('<button class="btn btn-default%s' + '" type="button" name="advancedSearch" title="%s">', that.options.iconSize === undefined ? '' : ' btn-' + that.options.iconSize, that.options.formatAdvancedSearch()));
        html.push(sprintf('<i class="%s %s"></i>', that.options.iconsPrefix, that.options.icons.advancedSearchIcon))
        html.push('</button></div>');

        /*that.$toolbar.prepend(html.join(''));

        that.$toolbar.find('button[name="advancedSearch"]')
            .off('click').on('click', function() {
                showAvdSearch(that.options.columns, that.options.formatAdvancedSearch(), that.options.formatAdvancedCloseButton(), that);
            });*/
        
       
        $search=$(html.join('')).appendTo($btnGroup);
        that.$toolbar.find('button[name="advancedSearch"]')
        .off('click').on('click', function() {
            showAvdSearch(that.options.columns, that.options.formatAdvancedSearch(), that.options.formatAdvancedCloseButton(), that);
        });
        
    };

    BootstrapTable.prototype.load = function(data) {
        _load.apply(this, Array.prototype.slice.apply(arguments));

        if (!this.options.advancedSearch) {
            return;
        }

        if (typeof this.options.idTable === 'undefined') {
            return;
        } else {
            if (!firstLoad) {
                var height = parseInt($(".bootstrap-table").height());
                height += 10;
                $("#" + this.options.idTable).bootstrapTable("resetView", {height: height});
                firstLoad = true;
            }
        }
    };

}(jQuery);
