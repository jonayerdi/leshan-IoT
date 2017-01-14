
var address = "localhost:8080";
var RS;
var GS;
var BS;
var $endpoint;
var $lowLight;

function lowLight() {
	$.ajax({
		url: 'http://'+address+'/api/lights/'+$endpoint.val()+'/6/set?value='+$lowLight.prop('checked'),
		success: function(data) {
			
		},
		failure: function(data) { 
			
		}
	}); 
}

function colorChange() {
	var newValue = "("+RS.slider('getValue')+","+GS.slider('getValue')+","+BS.slider('getValue')+")";
	$.ajax({
		url: 'http://'+address+'/api/lights/'+$endpoint.val()+'/5/set?value='+newValue,
		success: function(data) {
			
		},
		failure: function(data) { 
			
		}
	}); 
}

$(document).ready(function() {
	$endpoint = $('#endpoint');
	RS = $('#RSlider').slider();
	RS.on('slide', function(ev){
		colorChange();
	});
	GS = $('#GSlider').slider();
	GS.on('slide', function(ev){
		colorChange();
	});	
	BS = $('#BSlider').slider();
	BS.on('slide', function(ev){
		colorChange();
	});	
	$lowLight = $('#lowLight');
});