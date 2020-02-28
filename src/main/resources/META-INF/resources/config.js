;(function(){
AUI().applyConfig({
	groups: {
		mapbase: {
			
			base: MODULE_PATH + '/js/',
			combine: Liferay.AUI.getCombine(),
			modules:{
				'liferay-map-common': {
					path: '/js/ramalFacil.js',
					requires: ['aui-base']
				}
			},
			root: MODULE_PATH + '/js/'
		}
}
}
);	
})();