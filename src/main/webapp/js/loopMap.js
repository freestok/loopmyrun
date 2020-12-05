// add map components
const mp = L.map('map').setView([15.9451, 15.468], 2);
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

function onSubmit(event) {
	event.preventDefault();
	let data = {
		lat: marker._latlng.lat,
		lng: marker._latlng.lng,
		distance: $('#userDistance').val(),
		unit: $('input[name="distanceUnit"]:checked').val()
	}
	$.ajax({
		type: "POST",
		url: "/getLoop",
		data: data,
		success: showRoutes,
		error: (err) => alert(err)
	});
}
// functions

function showRoutes(data) {
	L.geoJSON(data).addTo(mp);
}

function addPoint(latlng) {
	if (marker) mp.removeLayer(marker);
	let msg = `(${latlng.lat.toFixed(2)}, ${latlng.lng.toFixed(2)})`
	marker = L.marker(latlng, {draggable: true}).addTo(mp);
	marker.on('dragend', (event) => {
		let latlng = event.target._latlng;
		let msg = `(${latlng.lat.toFixed(2)}, ${latlng.lng.toFixed(2)})`
		$('#userLoc').val(msg);
	});
	$('.leaflet-container').css('cursor','');
	$('#userLoc').val(msg);
}

function mapClick(event) {
	if (marker) mp.removeLayer(marker);
	addPoint(event.latlng);

}
function mouseEnter(event) {
	if (marker) {
		$('.leaflet-container').css('cursor','');
	} else {
		$('.leaflet-container').css('cursor','crosshair');
	}
}



