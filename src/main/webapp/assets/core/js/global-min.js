/*
 * 全局应用管理。
 * 在使用前必须先加载 jquery-v1.9.1 库。
 * 
 * @version 1.0.1
 * @author dudl
 * @date 2014-05-05
 */


/*
 * 定义全局变量 Rt 和其它。
 * 
 * @date 2014-05-05
 * @author dudl@zjrealtech.com
 */
var
/**
 * Rt 变量为网站的总应用命名空间，之后会再对该命名空间进行扩展，
 * 如 Rt.util、Rt.Overlay、Rt.statusCode 等。
 * 
 * @date 2014-05-05
 * @author dudl@zjrealtech.com
 */
Rt = window.Rt || {}
;


((function fnRt() {
    var
	/**
	 * URL 时间戳。
	 * 
	 * @private
	 */
	_tag = "20150817",	// 线上使用。


	/**
	 * 当前主机信息。
	 */
	_host = location.protocol + "//" + location.hostname + (!location.port ? "" : ":" + location.port) + "/",


	/**
	 * 资源路径。
	 */
	_assets = function() {
		if (window.ASSETS_DOMAIN) {
			return ASSETS_DOMAIN;
		} else {
			var sOrigin = location.origin,
				sPort = location.port,
				
				sAssetsHost = sOrigin + (!sPort ? "" : ":" + sPort) + "/assets/"
			;
			
			
			return sAssetsHost;
		}
	}(),


	/**
	 * 简单的对 jQuery 进行扩展。
	 * 
	 * @method
	 * @return {Object}
	 */
	_extendJQuery = function() {
		/**
		 * 记录已加载过的列表。
		 * 每次都会先来这验证某地址是否已成功加载。
		 * URL 需完成一致。
		 * 
		 * 数据为数组的数组，[地址，参数，结果集]。
		 */
		var aaoCachedJSONData = [
//			[sUrl, sParam, aoData]
		];


		/**
		 * 根据 URL 地址和参数，查询是否已加载过。
		 * 
		 * @param {String} sUrl
		 * @param {Object} oParam
		 * @return {Object}
		 */
		function getCachedJSONData(sUrl, oParam) {
			// 把参数转换成字符串。
			var sParam = $.param(oParam);

			var aoData = null;
			var aoItem = null;


			// 循环检测。
			for (var i = aaoCachedJSONData.length - 1; i >= 0; i--) {
				aoItem = aaoCachedJSONData[i];

				// [URL, 参数, 数据集]。
				if (aoItem[0] == sUrl && aoItem[1] == sParam) {
					aoData = aoItem[2];

					break;
				}
			}


			return aoData;
		};


		/**
		 * 对 jQuery 的扩展对象。
		 */
		var oExtends = {
			/**
			 * 版本标记（时间戳功能）。
			 * 
			 * @type {String}
			 */
			tag: _tag,


			/**
			 * 扩展 jsonp 方法。
			 * 
			 * @method
			 * @param {String} sUrl 要请求的地址。
			 * @param {Object|undefined} oData url 中的参数。
			 * @param {Function|undefined} fnSuccess 成功回调函数。
			 * 	#param {Object} 接口返回的统一格式对象。
			 * 	#return  {void}
			 * @param {Object|undefined} oSettings ajax 其它配置项。
			 * 
			 * 
			 * 参数传递还可以以以下形式传递。
			 * 
			 * 第二种参数方式。
			 *	#param {String}
			 *	#param {fnSuccess|undefined}
			 *	#param {Object|undefined}
			 * 
			 * @return {jQuery}
			 */
			jsonp: function(sUrl, oData, fnSuccess, oSettings) {
				if ($.isFunction(oData)) {	// 第二种传参方式。
					oSettings = fnSuccess;
					fnSuccess = oData;
					oData = null;
				}

				// 参数合并。
				oSettings = $.extend({
					url: sUrl,
					data: oData,
					dataType: "jsonp",
					success: fnSuccess
				}, oSettings || {});

				return $.ajax(oSettings);
			},


			/**
			 * 加载有可能存在的数据。
			 * 
			 * 参数传递：
			 * {String}, {Object}, {Function}
			 * {String}, {Function}
			 * {String}
			 * 
			 * @param {String} sUrl
			 * @param {Object|Function|undefined} oParam
			 * @param {Function|undefined} fnCallback
			 * @return {void}
			 */
			getCachedJSON: function(sUrl, oParam, fnCallback) {
				/*
				 * 检测是否使用 iframe 框架，判断当前是否与顶层页一致。
				 * 如果一致则不需要特殊处理，不一致需要向上取到最顶层的同域父页面。
				 */
				if (self != top) {
					// 检测顶层页面与当前页面是否是同域的。
					var bIsSameDomain = true;
					try {
						top.document;
					} catch (oError) {
						// 操作抛出异常，表示非同域的。
						bIsSameDomain = false;
					}


					// 判断上述结构结果。
					if (bIsSameDomain) {
						// 同域的，可直接使用。

						top.$.getCachedJSON.apply(this, arguments);
						// 直接返回。
						return;
					} else {
						// 非同域的。

						// 向上取出最顶层的同域父页面。
						var oParent = self;
						while (oParent.parent != top) {
							try {
								// 尝试操作父页面对象，如果不抛出异常则是同域安全的。
								oParent.parent.document;
								oParent = oParent.parent;
							} catch (oError) {
								// 得到异常，表示是跨域的，退出查询。
								break;
							}
						}


						/*
						 * 从上述查找同域父页面结果分析，当前页与查询的页面是否是同一个。
						 * 如果是同一个则不特殊处理，不是同一个页面则表示找到了最顶层的父页面，使用父页面的方法来加载。
						 */
						if (self != oParent) {
							oParent.$.getCachedJSON.apply(this, arguments);
							return;
						}
					}
				}


				// 检测参数类型。
				if ($.isFunction(oParam)) {
					fnCallback = oParam;
					oParam = {};
				}

				if (!oParam) {
					oParam = {};
				}


				// 检测当前地址和参数是否已加载过。
				var oCachedData = getCachedJSONData(sUrl, oParam);
				if (oCachedData) {
					// 取已加载过的数据直接回调返回。
					fnCallback && fnCallback(oCachedData);
		
					return;
				}


				// 未加载过，发起请求。
				$.getJSON(sUrl, oParam, function(oAjaxData) {
					// 保存已加载的数据。
					aaoCachedJSONData.push([
						sUrl, $.param(oParam), oAjaxData
					]);


					// 回调。
					fnCallback && fnCallback(oAjaxData);
				});
			},


			/**
			 * 同步加载脚本集。
			 * 
			 * @param {Array} asUrls 要加载的脚本地址列表。
			 * @param {Function|undefined} fnSuccess 全部成功回调。
			 * @return {void}
			 */
			getScripts: function(asUrls, fnSuccess) {
				var abLoaded = [];

				// 如果是单个字符串参数，转换成数组。
				if ($.isString(asUrls)) {
					asUrls = [asUrls];
				}


				// 循环加载所有要引入的脚本。
				asUrls.forEach(function(sUrl, nIndex) {
					abLoaded[nIndex] = false;
					// 发起同步加载。
					createScript(sUrl, function () {
						// 标记当前项加载成功。
						abLoaded[nIndex] = true;

						// 验证加载情况。
						checkLoaded(fnSuccess);
					});
				});


				/**
				 * 创建要引入的脚本。
				 * 
				 * @param {String} sUrl
				 * @param {Function} fnLoad
				 *  #param {Event}
				 *  #return {void}
				 * @return {void}
				 */
				function createScript(sUrl, fnLoad) {
					// 创建 script 标签。
					var oScript = document.createElement("script");
					oScript.src = sUrl;
					oScript.async = false;
					oScript.onload = fnLoad;

					// 添加到页头或页主体。
					(document.head || document.body).appendChild(oScript);
				}


				/**
				 * 检测是否都加载成功了，如果都成功后则再调用成功函数。
				 * 
				 * @param {Function|undefined} fnSuccess 全部成功后的回调函数。
				 * @return {void}
				 */
				function checkLoaded(fnSuccess) {
					for (var i = abLoaded.length - 1; i >= 0; i--) {
						if (!abLoaded[i]) {
							return;
						}
					}

					fnSuccess && fnSuccess();
				}
			},


			/**
			 * 同步加载脚本。
			 * 
			 * @param {String} sUrl 要加载的脚本地址。
			 * @param {Function|undefined} fnSuccess 成功回调。
			 * @param {Object|undefined} oSettings 其它配置。
			 * @return {Element}
			 */
			syncGetScript: function(sUrl, fnSuccess, oSettings) {
//				// 创建 script 标签。
//				var oScript = document.createElement("script");
//				oScript.src = sUrl;
//				oScript.async = false;
//				oScript.onload = fnSuccess;
//
//				// 添加到页头或页主体。
//				(document.head || document.body).appendChild(oScript);
//
//
//				return oScript;


				// 参数合并。
				oSettings = $.extend({
					url: sUrl,
					dataType: "script",
					async: false,
					success: fnSuccess,
					cache: true	// 启用缓存功能。
				}, oSettings || {});

				return $.ajax(oSettings);
			},


			/**
			 * 加载样式文件到 head 中。
			 * 如果某样式在 3 秒钟内未能加载成功，则会主动以超时处理，并主动调用回调函数。
			 * 
			 * @param {String} sSrc
			 * @param {Function|undefined} fnSuccess 仅非 IE 浏览器仅支持较新版本的。
			 *  #param {Boolean} 返回当前是 load 调用的回调还是超时主动调用的， load: true, timeout: false。
			 * @return {jQuery} 创建的 link 对象。
			 */
			getStyle: function(sSrc, fnSuccess) {
				var nTimeout = setTimeout(function() {
					if (!bIsLoad) {
						fnSuccess && fnSuccess(false); // timeout 方式回调。
						bIsLoad = true;
					}
				}, 3 * 1000),
					bIsLoad = false
				;

				return $('<link href="' + sSrc + '" rel="stylesheet" />')
					.appendTo("head")

					// onload 事件非 IE 浏览器仅较新的版本才支持。
					.on("load", function() {
						if (!bIsLoad) {
							clearTimeout(nTimeout);
							fnSuccess && fnSuccess(true); // load 方式回调。
							bIsLoad = true;
						}
					})
				;
			},


			/**
			 * 占位符替换工厂。
			 * 
			 * @method
			 * @param {String} sContent 含占位符的字符串。
			 * 	当要被替换的内容中含未知替换数据，则会保留当前点位符。
			 * @param {Object} oData 要替换的点位符数据，依据对象的键名与点位符一一对应，功能类似 KISSY.substitute。
			 * @param {String} sUnValue 当值为空、null、NaN 等无效值时，使用该值来替代，如果该值也无效，则会继续使用原来的值。
			 * @return {String} 返回替换后的字符串。
			 */
			substitute: function(sContent, oData, sUnValue) {
				if (!oData) {
					return sContent;
				}

				var sValue = "";
				for (var p in oData) {
					sValue = oData[p];
					if (!sValue && sUnValue != undefined) {
						sValue = sUnValue;
					}

					sContent = sContent.replace(new RegExp("\\{" + p + "\\}", "g"), sValue);
				}

				return sContent;
			},


			/**
			 * 将字数符反参数解决成对象。
			 * 与 $.param 相对。
			 * 
			 * @method
			 * @param {String} sParams
			 * @return {Object}
			 */
			unparam: function(sParams) {
				var asParams = (sParams || "").split("&"),
					oParams = {},
					asSplitedParam = []
				;

				for (var i = asParams.length - 1; i >= 0; i--) {
					asSplitedParam = asParams[i].split("=");
					oParams[asSplitedParam[0]] = asSplitedParam[1] || "";
//					var nIndex = asParams[i].indexOf("=");
//					oParams[asParams[i].substring(0,nIndex)] = asParams[i].substr(nIndex+1) || "";
				}

				return oParams;
			},
			
			
			/**
			 * 检测当前对象是否是 jQuery 对象。
			 * 
			 * @method
			 * @param {Object} oTester
			 * @return {Boolean}
			 */
			isJQuery: function(oTester) {
				try {
					return $.isElement(oTester.get(0));
				} catch (e) {
					return false;
				}
			},


			/**
			 * 是否是 DOM 节点。
			 * 
			 * @method
			 * @param {Object} oTester
			 * @return {Boolean}
			 */
			isElement: function(oTester) {
				try {
					var nNodeType = oTester.nodeType, // 可选的 DOM 标签类型。
					oTypes = {
						element: 1,
						attribute: 2,
						text: 3,
						comment: 8,
						documnet: 9
					}, bIsNode = false;

					// 逐一检测是否是其中一种标签类型。
					for (var p in oTypes) {
						if (oTypes[p] == nNodeType) {
							bIsNode = true;
							break;
						}
					}

					return bIsNode;
				} catch (oError) {
					return false;
				}
			},


			/**
			 * 检测当前对象是否是数字的。
			 * 
			 * @method
			 * @param {Object} oTester
			 * @return {Boolean}
			 */
			isNumber: $.isNumeric,


			/**
			 * 检测当前对象是否是字符串的。
			 * 
			 * @method
			 * @param {Object} oTester
			 * @return {Boolean}
			 */
			isString: function(oTester) {
				return $.type(oTester) === "string";
			},


			/**
			 * 检测当前对象是否是日期型的。
			 * 
			 * @method
			 * @param {Object} oTester
			 * @return {Boolean}
			 */
			isDate: function(oTester) {
				return $.type(oTester) === "date";
			},


			/**
			 * 检测当前对象是否是布尔型的。
			 * 
			 * @method
			 * @param {Object} oTester
			 * @return {Boolean}
			 */
			isBoolean: function(oTester) {
				return $.type(oTester) === "boolean";
			},


			/**
			 * 检测当前对象是否是对象型的。
			 * 
			 * @method
			 * @param {Object} oTester
			 * @return {Boolean}
			 */
			isObject: function(oTester) {
				return $.type(oTester) === "object";
			},


			/**
			 * 检测当前对象是否是 undefined 型的。
			 * 
			 * @method
			 * @param {Object} oTester
			 * @return {Boolean}
			 */
			isUndefined: function(oTester) {
				return $.type(oTester) === "undefined";
			},


			/**
			 * 检测当前对象是否 null 值。
			 * 
			 * @method
			 * @param {Object} oTester
			 * @return {Boolean}
			 */
			isNull: function(oTester) {
				return $.type(oTester) === "null";
			},


			/**
			 * 检测当前对象是否是正则。
			 * 
			 * @method
			 * @param {Object} oTester
			 * @return {Boolean}
			 */
			isRegExp: function(oTester) {
				return $.type(oTester) === "regexp";
			},


			/**
			 * 将父类拥有的属性添加到子类，不包括原型方法。
			 * 
			 * @param {Class} cSub 子类，会被修改。
			 * @param {Class} cSuper 父类。
			 * @param {Array|undefined} aoList 名单列表。
			 * @param {Boolean|undefined} bWhite 是否使用白名单，否则使用黑名单。
			 * @return {void}
			 */
			addOwerPrototype: function(cSub, cSuper, aoList, bWhite) {
				// 参数有效性参数。
				if (!aoList) {
					aoList = [];
				}

				bWhite = !!bWhite;


				// 遍历所有属性（包括原型）。
				for (var p in cSuper) {
					// 检测包含的属性，不包括原型。
					if (cSuper.hasOwnProperty(p)) {
						
						// 判断是使用白名单还是黑名单。
						if (bWhite && aoList.indexOf(p) != -1
							|| !bWhite && aoList.indexOf(p) == -1) {

							// 添加至子类。
							cSub[p] = cSuper[p];
						}
					}
				}
			},
						
			/**
			 * 添加鼠标滚轮事件。兼容firefox
			 * @param {Object} el
			 * @param {Object} type
			 * @param {Object} fn
			 * @param {Object} capture
			 * @return {void}
			 */
			addWheelEvent: function(el, type, fn, capture) {        
		        var _eventCompat = function(event) {
		            var type = event.type;
		            if (type == 'DOMMouseScroll' || type == 'mousewheel') {
		                event.delta = (event.wheelDelta) ? event.wheelDelta / 120 : -(event.detail || 0) / 3;
		            }
		            //alert(event.delta);
		            if (event.srcElement && !event.target) {
		                event.target = event.srcElement;    
		            }
		            if (!event.preventDefault && event.returnValue !== undefined) {
		                event.preventDefault = function() {
		                    event.returnValue = false;
		                };
		            }

		            return event;
		        };
		        if (window.addEventListener) {
	                if (type === "mousewheel" && document.mozHidden !== undefined) {
	                    type = "DOMMouseScroll";
	                }
	                el.addEventListener(type, function(event) {
	                    fn.call(this, _eventCompat(event));
	                }, capture || false);
		        } else if (window.attachEvent) {
	                el.attachEvent("on" + type, function(event) {
	                    event = event || window.event;
	                    fn.call(el, _eventCompat(event));    
	                });
		        }  				
			}
		};


		// 扩展到 jQuery。
		$.extend(jQuery, oExtends);


		return _rt;
	},


    /**
     * 设置组件“包”目录。
     * 
     * @date 2014-05-05
     * @author dudl@zjrealtech.com
     */
    _setPackage = function() {
		// 设置 seajs 的“包”。
		S.config({
			paths: {
				"util": _assets + "core/jquery/util",
				"vue": _assets + "core/vue/2.3.0",
				"adminlte": _assets + "core/adminlte/2.3.11/plugins",
				"echarts": _assets + "core/jquery/util/echarts/2.2.7",

				// 定义 3.1.3 的主题引用路径。
				"echarts-3-theme": _assets + "core/jquery/util/echarts/3.1.10/theme"
			},
			// 定义以别名方式引用资源。
			alias: {
				/*
				 * 3.1.3 版 echarts。
				 * $.use("util/echarts-3");
				 */
				"echarts-3": _assets + "core/jquery/util/echarts/3.1.10/echarts-min.js",
			},
			base: _assets
		});

        return _rt;
    };


	// 以下为 public 内容。


	/**
	 * 关心网命名空间内部对象。
	 * 
	 * @date 2014-05-05
	 * @author dudl@zjrealtech.com
	 */
	var _rt = {
		/**
		 * 当前使用的是否是开发调试模式。(false)
		 * 
		 * @property
		 * @static
		 * @type {Boolean}
		 */
		debugMode: true,


		/**
		 * URL 时间戳，URL 参数以 _t 标注。
		 * 
		 * @property
		 * @static
		 * @type {String}
		 */
		TAG: _tag,


		/**
		 * 当前主机信息。
		 * 
		 * @property
		 * @static
		 * @type {String}
		 */
		HOST: _host,


		/**
		 * 资源路径。
		 * 
		 * @property
		 * @static
		 * @type {String}
		 */
		ASSETS: _assets,


		/**
		 * 接口中心主机地址。
		 * 
		 * @property
		 * @static
		 * @type {String}
		 */
		API_HOST: "",
		
		/**
		 * webscada 命名空间。
		 */
		ws: {}
	};


	// 将内部命名空间对象赋到全局对象下。
	Rt = _rt;


	// 简单扩展 jQuery 。
	_extendJQuery();
	// 设置包路径。
	_setPackage();


	// 将内部定义的对象添加到 jQuery 中。
	$.extend(true, jQuery, Rt);
}))();
