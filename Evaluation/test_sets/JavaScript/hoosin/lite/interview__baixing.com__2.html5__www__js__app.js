define([
    'jquery',
	'tools/clickFeedback',
    'plugins/jquery.placeholder/main',
    'tools/testInputAttr'
],
function($, clickFeedback, placeholder, testInputAttr){
    $(document).ready(function(){
		//兼容HTML5 input新属性placeholder
        placeholder();
        
		if( ! testInputAttr('required')){
			//兼容HTML5 input新属性required
			$(document).delegate('form:not([novalidate])', 'submit', function(evt) {
				if( ! $(this).find(':submit[formnovalidate]').length)
				{
					var res = true;
					$(this).find('input[required]').each(function(){
						var that = $(this);

						//只判断长度，空格也算有值
						if( ! that.val().length){
							res = false;
	                        that.focus();
	                        /*
	                         * @todo 可以将该功能封装为一个独立的UI组件
	                         *  - Overlay.js 基础浮层组件，提供浮层显示隐藏、定位
	                         *      - Widget.js  UI 组件的基础类，约定组件的基本生命周期，实现一些通用功能
	                         *      - Position.js  简单实用的定位工具，将一个 DOM 节点相对于另一个 DOM 节点进行定位操作
	                         */
							var eHeight = that.outerHeight();
							var offsetTop = that.offset().top + (eHeight ? eHeight : 12);
							var offsetLeft = that.offset().left + (that.width() / 2);
							var floatBox = $('<div style="padding: 5px; border: 1px solid #ccc; background-color: #ffffff; color: red;position: absolute; top: '+offsetTop+'px; left: '+offsetLeft+'px;">不能为空</div>');
							$('body').append(floatBox);
							
							clickFeedback(floatBox, function(){
	                            if(this.is(':visible')){
	                                this.fadeOut().remove();
								}
	                        });
							
							return false;
						}
					});
					if( ! res){
						evt.preventDefault();
					}
				}
			});
		}

        /*兼容HTML5 input新属性autofocus
        if( ! testInputAttr('autofocus')){
            //这是一个不完善的兼容方案，考虑这种情况：JS还未解释完毕，用户已经在输入框上进行输入；该方法会打断用户的输入。
            $('form:last').find('input[autofocus]:last').focus();
        }*/
    });
});
