AUI().ready(function(A){
	var portletNameSpaceRamalFacil = '<portlet:namespace/>';

	A.all('#'+portletNameSpaceRamalFacil+'btnMeuRamal').each(function(node){
		node.on('click',function(node){
			modalRamalFacil({titulo:'Meu Ramal',url:urlMyAccount});
		});
	});
	
	A.all('.'+portletNameSpaceRamalFacil+'btnGerenciarUsuario').each(function(node){
		node.on('click',function(btn){
			modalRamalFacil({
				titulo:	btn.currentTarget._node.attributes['data-title'].value,
				url:	urlGerenciarUsuario + btn.currentTarget._node.attributes['data-login'].value
			});
		});
	});
	
	
});

function modalRamalFacil(param){
	Liferay.Util.openWindow(
			{
				cache: false,	
				dialog: {
					align: Liferay.Util.Window.ALIGN_CENTER,
					modal:true,
					width:450,
					height:530,
					on:{
						close: function(){
							window.location.reload();
						}
					}
				},
				id: 'classificadosIframeDialog',
				title: param.titulo,
				uri: param.url
				
			}
		);
}