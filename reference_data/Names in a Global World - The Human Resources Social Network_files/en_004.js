
   /*
 * jQuery.bind-first library v0.2.2
 * Copyright (c) 2013 Vladimir Zhuravlev
 *
 * Released under MIT License
 * @license
 *
 * Date: Tue Jan  7 20:59:23 ICT 2014
 **/
(function(t){function e(e){return f?e.data("events"):t._data(e[0]).events}function n(t,n,r){var i=e(t),a=i[n];if(!f){var s=r?a.splice(a.delegateCount-1,1)[0]:a.pop();return a.splice(r?0:a.delegateCount||0,0,s),void 0}r?i.live.unshift(i.live.pop()):a.unshift(a.pop())}function r(e,r,i){var a=r.split(/\s+/);e.each(function(){for(var e=0;a.length>e;++e){var r=t.trim(a[e]).match(/[^\.]+/i)[0];n(t(this),r,i)}})}var i=t.fn.jquery.split("."),a=parseInt(i[0]),s=parseInt(i[1]),f=1>a||1==a&&7>s;t.fn.bindFirst=function(){var e=t.makeArray(arguments),n=e.shift();return n&&(t.fn.bind.apply(this,arguments),r(this,n)),this},t.fn.delegateFirst=function(){var e=t.makeArray(arguments),n=e[1];return n&&(e.splice(0,2),t.fn.delegate.apply(this,arguments),r(this,n,!0)),this},t.fn.liveFirst=function(){var e=t.makeArray(arguments);return e.unshift(this.selector),t.fn.delegateFirst.apply(t(document),e),this},f||(t.fn.onFirst=function(e,n){var i=t(this),a="string"==typeof n;if(t.fn.on.apply(i,arguments),"object"==typeof e)for(type in e)e.hasOwnProperty(type)&&r(i,type,a);else"string"==typeof e&&r(i,e,a);return i})})(jQuery);
