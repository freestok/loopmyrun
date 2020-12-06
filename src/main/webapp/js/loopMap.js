// add map components
// const mp = L.map('map').setView([15.9451, 15.468], 2);
const mp = L.map('map').setView([42.962, -85.639],18);
const provider = new GeoSearch.EsriProvider();
const searchControl = new GeoSearch.GeoSearchControl({
  provider: provider,
  style: 'bar',
});
L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}', {
	attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ, TomTom, Intermap, iPC, USGS, FAO, NPS, NRCAN, GeoBase, Kadaster NL, Ordnance Survey, Esri Japan, METI, Esri China (Hong Kong), and the GIS User Community'
}).addTo(mp);
mp.addControl(searchControl);

let marker;

// listeners
mp.on('mouseover', mouseEnter);
mp.on('click',mapClick);
mp.on('geosearch/showlocation', (event) => {
	console.log(event);
	let latlng = event.marker._latlng;
	addPoint(latlng);
	mp.removeLayer(event.marker);
});

$('#startOver').click(() => {
	mp.removeLayer(marker);
	marker = null;
});

$("#loopForm").submit(onSubmit);
$('#formTab').click(() => {
	console.log('click!');
	$('#loopForm').show();
	$('#loopResults').hide();
	$('#resultsTab').removeClass('active');
	$('#formTab').addClass('active');
});
$('#resultsTab').click(() => {
	console.log('click');
	$('#loopResults').show();
	$('#loopForm').hide();
	$('#resultsTab').addClass('active');
	$('#formTab').removeClass('active');
});

function onSubmit(event) {
	event.preventDefault();
	$("#loopResults").empty();
	let dist = convertDist($('#userDistance').val());
	console.log(dist);
	let data = {
		lat: marker._latlng.lat,
		lng: marker._latlng.lng,
		distance: dist
	}
	$.ajax({
		type: "POST",
		url: "/getLoop",
		data: data,
		success: showRoutes,
		error: (err) => alert("Oops, an unknown error occurred. I'm just as confused as you are.",err)
	});
}

function convertDist(dist) {
	let unit = $('input[name="distanceUnit"]:checked').val();
	if (unit == 'mile') return dist * 1609.344;
	else if (unit == 'km') return dist * 1000;
}

function metersToUnit(f) {
	let unit = $('input[name="distanceUnit"]:checked').val();
	if (unit == 'mile')  {
		let lineDistance = turf.lineDistance(f,{units:'miles'})
		let rounded = lineDistance.toFixed(2);
		return [lineDistance, `${rounded} mi.`];
	}
	else if (unit == 'km') {
		let lineDistance = turf.lineDistance(f);
		let rounded = lineDistance.toFixed(2);
		return [lineDistance, `${rounded} km.`];
	} 
}
function showRoutes(data) {
	mp.removeLayer(marker);
	marker = null;
	if (data.message == 'no loops') {
		alert("I couldn't find any loops for this area!")
		return null;
	}
	console.log('data',data);
	let goalLength = convertDist($('#userDistance').val());
	let features = data.features;
	features.sort((a, b) => {
		return Math.abs(goalLength-a.properties.ln) - Math.abs(goalLength-b.properties.ln);
	});

	// check for duplicates
	let geometries = [];
	let forAdd = [];

	for (feature of features) {
		if (geometries.includes(feature.properties.ln)) continue;
		else forAdd.push(feature);
		geometries.push(feature.properties.ln);
	}
	forAdd = forAdd.slice(0, 3);
	console.log('forAdd',forAdd);
	
	for (let i=0; i < forAdd.length; i++) {
		let add = forAdd[i]
		console.log('feature',add);
		let convertedLength = metersToUnit(add);
		let dist=Math.floor(convertedLength[0]);
		let title = `Route #${i+1}`;
		if (i == 0) {
			cardClass = 'card text-white bg-info mb-3';
			buttonColor = 'btn-light'
		}
		else {
			cardClass = 'card bg-light mb-3';
			buttonColor = 'btn-primary';
		}
		let msg = `
		<div class="${cardClass}" style="width: 12rem;">
			<div class="card-body">
				<h5 class="card-title">${title}</h5>
				<h6 class="card-subtitle mb-3">${convertedLength[1]}</h6>
				<a href="#" class="btn ${buttonColor}">Save Route</a>
			</div>
		</div>
		`
		$('#loopResults').append(msg);

		console.log('RESULT');
		console.log('dist',dist);
		
		for(let step=1;step<dist+1;step++){
			console.log('STEP');
			let res = turf.along(add, step,{units:'miles'});
			let coords = res.geometry.coordinates;
			var numberIcon = L.icon({
				iconUrl: `assets/${step}.png`,
				iconSize: [25, 25], 
			});
			L.marker([coords[1], coords[0]], {icon: numberIcon}).addTo(mp);

			console.log(res);
			// L.geoJson(res, {
			// 	pointToLayer: function(feature, latlng) {
			// 	  console.log(latlng, feature);
			// 	  return L.marker(latlng, {
			// 		icon: smallIcon
			// 	  });
			// 	},
			// 	onEachFeature: onEachFeature
			//   }).addTo(mp);
			// result.features.push();
		}
		L.geoJson(add).addTo(mp);
		console.log('addRESULT');
	}

	// add lines and distances along line
	// let jsonLine = forAdd[0];
	// jsonLine = L.geoJson(jsonLine);
	// let length = turf.lineDistance(jsonLine, 'miles');
	


	// let routes = L.geoJSON(data);
	// console.log(routes);
	$('#resultsTab').removeClass('disabled');
	$('#resultsTab').addClass('active');
	$('#formTab').removeClass('active');
	$('#loopForm').hide();
	$('#loopResults').show();
	// routes.addTo(mp);
}

function addPoint(latlng) {
	if (marker) mp.removeLayer(marker);
	let msg = `(${latlng.lat.toFixed(3)}, ${latlng.lng.toFixed(3)})`
	marker = L.marker(latlng, {draggable: true}).addTo(mp);
	marker.on('dragend', (event) => {
		let latlng = event.target._latlng;
		let msg = `(${latlng.lat.toFixed(3)}, ${latlng.lng.toFixed(3)})`
		$('#userLoc').val(msg);
	});
	$('.leaflet-container').css('cursor','');
	$('#userLoc').val(msg);
}

function mapClick(event) {
	// resultsTab
	let resultsClass = $('#resultsTab').attr("class").split(/\s+/);
	if (resultsClass.includes('active')) return null;

	if (marker) mp.removeLayer(marker);
	addPoint(event.latlng);

}
function mouseEnter(event) {
	if (marker) {
		$('.leaflet-container').css('cursor','');
	} else {
		let resultsClass = $('#resultsTab').attr("class").split(/\s+/);
		if (resultsClass.includes('active')) return null;
		$('.leaflet-container').css('cursor','crosshair');
	}
}



