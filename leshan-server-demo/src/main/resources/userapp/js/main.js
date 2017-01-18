
var address = "Equipo:8080";
var RS;
var GS;
var BS;
var $endpoint;
var $lowLight;
var endpointsUpdateIntervalID;

function updateLightValues() {
	$.ajax({
		url: 'http://'+address+'/api/lights/'+$endpoint.val()+'/6',
		success: function(data) {
			var json = eval(data);
			$lowLight.prop('checked',json["Low Light"])
		},
		failure: function(data) { 
			
		}
	});
	$.ajax({
		url: 'http://'+address+'/api/lights/'+$endpoint.val()+'/5',
		success: function(data) {
			var json = eval(data);
			var color = json["Light Color"];
			var colors = color.substring(1,color.length-1).split(',');
			RS.slider('setValue',colors[0]);
			GS.slider('setValue',colors[1]);
			BS.slider('setValue',colors[2]);
		},
		failure: function(data) { 
			
		}
	});
}

function visualizeEndpoints(json) {
	$endpoints_list.empty();
	for(var i = 0 ; i < json.length ; i++) {
		var client = json[i];
		var e = client["endpoint"];
		//Update view
		var list_item = "<li class=\"list-group-item\" onclick=\"$endpoint.val('"+e+"'); updateLightValues();\">"+e+"</li>";
		$endpoints_list.append(list_item);
	}
}

function updateEndpoints() {
	$.ajax({
		url: 'http://'+address+'/api/lights',
		success: function(data) {
			var json = eval(data);
			visualizeEndpoints(json);
		},
		failure: function(data) { 
			
		}
	}); 
}

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
	$endpoints_list = $('#endpoints_list');
	//Set interval for endpoints list update
	endpointsUpdateIntervalID = setInterval(function(){updateEndpoints()}, 500);
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