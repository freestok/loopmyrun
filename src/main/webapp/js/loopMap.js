// add map components
// const mp = L.map('map').setView([15.9451, 15.468], 2);
const mp = L.map('map').setView([42.962, -85.639],18);
let popup;


// check to see if accessing saved route
let urlString = window.location.href
let url = new URL(urlString);
let saveId = url.searchParams.get("id");
if (saveId) {
	popup = L.popup({
		closeButton: true,
		autoClose: true
	})
		.setLatLng(mp.getBounds().getCenter())
		.setContent('<p>Loading your route...</p>')
		.openOn(mp);
	console.log("GET SAVED ROUTE");
	$.ajax({
		type: "GET",
		url: "/save",
		data: { id: saveId },
		success: showSavedRoute,
		error: (err) => {
			// clearInterval(checker);
			console.log("ERROR with saveID", err)
		}
	});
} else {
	popup = L.popup({
		closeButton: true,
		autoClose: true
	})
		.setLatLng(mp.getBounds().getCenter())
		.setContent(`<p>Welcome! Drop a point (or search for an address), enter your distance and unit, and find a loop near you.</p>
					 <p>If you get a wonky looking loop (or none at all), try moving your point slightly and trying again.`)
		.openOn(mp);
}

const provider = new GeoSearch.EsriProvider();
const searchControl = new GeoSearch.GeoSearchControl({
  provider: provider,
  style: 'bar',
});
L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}', {
	attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ, TomTom, Intermap, iPC, USGS, FAO, NPS, NRCAN, GeoBase, Kadaster NL, Ordnance Survey, Esri Japan, METI, Esri China (Hong Kong), and the GIS User Community'
}).addTo(mp);
mp.addControl(searchControl);

let marker, pause, uuid, checker;
let routesList = {};

// listeners
mp.on('mouseover', mouseEnter);
mp.on('click',mapClick);
mp.on('drag',()=>{
	if (!pause) mp.closePopup(popup);
});

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

function saveRoute(uid) {
	popup.setContent('<p>Saving Route...</p>')
		.setLatLng(mp.getBounds().getCenter())
		.openOn(mp);
	let route = routesList[uid];
	console.log("UUID: ", uuid);
	$.ajax({
		type: "POST",
		url: "/save",
		data: {route: JSON.stringify(route)},
		success: (d) => {
			mp.closePopup(popup);
			let save = `https://loopmyrun.herokuapp.com/?id=${d.id}`
			popup.setContent(`<p>Route Saved!</p> <p><a style="color:white;" href="${save}">${save}</a></p>`)
			.setLatLng(mp.getBounds().getCenter())
			.openOn(mp);
		},
		error: (err) => console.log("ERROR saveRoute()",err)
	});
}
function activateRoute(uid) {
	console.log('uid',uid);
	let route = routesList[uid];
	removeAll();
	displayRoute(route[0],route[1]);

	// reset links
	$('#route1link').html(`<a href="#" onclick="activateRoute('route1');return false;">Route #1</a>`);
	$('#route2link').html(`<a href="#" onclick="activateRoute('route2');return false;">Route #2</a>`);
	$('#route3link').html(`<a href="#" onclick="activateRoute('route3');return false;">Route #3</a>`);
	

	
	$('.card').attr('class','card bg-light mb-3');
	$('.route').attr('class','btn btn-primary route');

	let newTitle;
	switch(uid) {
		case('route1'): newTitle = 'Route #1';
		case('route2'): newTitle = 'Route #2';
		case('route3'): newTitle = 'Route #3';
	}

	$('#'+uid).attr('class','card text-white bg-info mb-3');
	$('#'+uid+'link').html(newTitle);
	$('#'+uid+'btn').attr('class','btn btn-light route');
}

function removeAll() {
	mp.eachLayer((layer) => {
		if (!layer.options.attribution) mp.removeLayer(layer);
	}); 
}
function onSubmit(event) {
	event.preventDefault();
	pause = true;

	// check marker
	if (!marker) {
		alert('Select a location, please.');
		mp.closePopup(mp);
		return null;
	}

	// check distance
	let userDist = $('#userDistance').val()
	let unit = $('input[name="distanceUnit"]:checked').val();
	if (unit === 'km' && userDist > 32) {
		alert('Your distance is too high! Try again.');
		mp.closePopup(mp);
		return null;
	} else if (unit === 'mile' && userDist > 20) {
		alert('Your distance is too high! Try again.');
		mp.closePopup(mp);
		return null;
	}
	
	removeAll();

	$("#loopResults").empty();
	let dist = convertDist();
	console.log(dist);
	uuid = uuidv4();
	let data = {
		lat: marker._latlng.lat,
		lng: marker._latlng.lng,
		distance: dist,
		uuid: uuid
	}
	$.ajax({
		type: "POST",
		url: "/getLoop",
		data: data
		// success: showRoutes,
		// error: (err) => console.log("Oops, an unknown error occurred. Try adjusting your point slightly.",err)
	});

	popup = L.popup({
		closeButton: true,
		autoClose: true
	})
		.setLatLng(mp.getBounds().getCenter())
		.setContent('<p>Finding route...</p>')
		.openOn(mp);

	checker = setInterval(retrieveTempRoute, 3000)
}

function retrieveTempRoute() {
	console.log("UUID: ", uuid);
	$.ajax({
		type: "GET",
		url: "/getLoop",
		data: {uuid: uuid},
		success: showRoutes,
		error: (err) => {
			// clearInterval(checker);
			console.log("ERROR retrieveTempRoute()",err)
		}
	});
}

function uuidv4() {
	return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
		(c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
	);
}

function convertDist() {
	let dist = $('#userDistance').val()
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

function closestFeatures(features) {
	let d = Number($('#userDistance').val())
	let userUnit = $('input[name="distanceUnit"]:checked').val();
	closest = [];
	let origin = [marker._latlng.lng, marker._latlng.lat];
	for (let feature of features) {
		// make sure route isn't too far away
		let startPoint = feature.geometry.coordinates[0]
		if (turf.distance(startPoint, origin) > .8) continue;
		feature.dist = metersToUnit(feature)[0];
		feature.unit = userUnit === 'mile' ? 'mi.' : 'km';
		closest.push(feature);
	}
	return closest.sort((a, b) => Math.abs(d- a.dist) - Math.abs(d - b.dist));
}

function showSavedRoute(data) {
	mp.closePopup(popup);
	console.log("DATA: ", data);
	let geom = data[0].geometry;
	let dist = data[0].dist;
	let unit = data[0].unit;
	let msg = `
	<div class="card text-white bg-info mb-3" style="width: 100%;">
		<div class="card-body">
			<h5 class="card-title">Saved Route</h5>
			<h6 class="card-subtitle mb-3">${dist.toFixed(2)} ${unit}</h6>
		</div>
	</div>
	`
	$('#loopResults').append(msg);
	$('#resultsTab').removeClass('disabled');
	$('#resultsTab').addClass('active');
	$('#formTab').removeClass('active');
	$('#loopForm').hide();
	$('#loopResults').show();
	pause = false;

	let c = geom.coordinates[0];
	let startIcon = L.icon({
		iconUrl: `assets/start.png`,
		iconSize: [25, 25], 
	});
	L.marker([c[1], c[0]], {icon: startIcon}).addTo(mp);
	for(let step=1;step<dist+1;step++){
		console.log('STEP');
		let res = unit === 'km' ? turf.along(geom, step) : turf.along(geom, step,{units:'miles'});
		let coords = res.geometry.coordinates;
		let numberIcon = L.icon({
			iconUrl: `assets/${step}.png`,
			iconSize: [25, 25], 
		});
		L.marker([coords[1], coords[0]], {icon: numberIcon}).addTo(mp);

		console.log(res);
	}
	let lyr = L.geoJson(geom).addTo(mp);
	mp.fitBounds(lyr.getBounds());

}

function showRoutes(data) {
	console.log("DATA showRoutes: ",data);
	clearInterval(checker);
	mp.closePopup(popup);

	// if no routes found
	if (data.message == 'no loops') {
		alert("I couldn't find any loops for this area!")
		return null;
	}
	
	let dataFeats = data.features;
	features = closestFeatures(dataFeats);

	// check for duplicates
	let geometries = [];
	let forAdd = [];
	for (let feature of features) {
		if (geometries.includes(feature.dist)) continue;
		else forAdd.push(feature);
		console.log('ADDING');
		geometries.push(feature.dist);
	}
	forAdd = forAdd.slice(0, 3);
	console.log('forAdd',forAdd);
	
	for (let i=0; i < forAdd.length; i++) {
		let add = forAdd[i]
		let convertedLength = metersToUnit(add);
		let dist=Math.floor(convertedLength[0]);
		let routeName = `Route #${i+1}`;
		let cardID = `route${i+1}`;
		let title;
		if (i == 0) {
			title = routeName;
			cardClass = 'card text-white bg-info mb-3';
			buttonColor = 'btn-light'
		}
		else {
			title = `<a href="#" onclick="activateRoute('${cardID}');return false;">${routeName}</a>`;
			cardClass = 'card bg-light mb-3';
			buttonColor = 'btn-primary';
		}
		let msg = `
		<div id="${cardID}" class="${cardClass}" style="width: 100%;">
			<div class="card-body">
				<h5 id="${cardID}link" class="card-title">${title}</h5>
				<h6 class="card-subtitle mb-3">${convertedLength[1]}</h6>
				<a href="#" id="${cardID}btn" onclick="saveRoute('${cardID}');return false;" class="btn ${buttonColor} route">Save Route</a>
			</div>
		</div>
		`
		$('#loopResults').append(msg);

		if (i == 0) displayRoute(add,dist);
		routesList[cardID] = [add, dist];
	}
	$('#resultsTab').removeClass('disabled');
	$('#resultsTab').addClass('active');
	$('#formTab').removeClass('active');
	$('#loopForm').hide();
	$('#loopResults').show();
	pause = false;
}

function displayRoute(add,dist) {
	let unit = $('input[name="distanceUnit"]:checked').val();
	let c = add.geometry.coordinates[0];
	let startIcon = L.icon({
		iconUrl: `assets/start.png`,
		iconSize: [25, 25], 
	});
	L.marker([c[1], c[0]], {icon: startIcon}).addTo(mp);
	for(let step=1;step<dist+1;step++){
		console.log('STEP');
		let res = unit === 'km' ? turf.along(add, step) : turf.along(add, step,{units:'miles'});
		let coords = res.geometry.coordinates;
		let numberIcon = L.icon({
			iconUrl: `assets/${step}.png`,
			iconSize: [25, 25], 
		});
		L.marker([coords[1], coords[0]], {icon: numberIcon}).addTo(mp);

		console.log(res);
	}
	let lyr = L.geoJson(add).addTo(mp);
	mp.fitBounds(lyr.getBounds());
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
	if(!pause) {
		mp.closePopup(popup);
		// resultsTab
		let resultsClass = $('#resultsTab').attr("class").split(/\s+/);
		if (resultsClass.includes('active')) return null;
		if (pause) return null;

		if (marker) mp.removeLayer(marker);
		addPoint(event.latlng);
	}
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



