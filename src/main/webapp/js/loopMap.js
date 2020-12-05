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

// map listeners
mp.on('mouseover', mouseEnter);
mp.on('click',mapClick)

function mapClick(event) {
	if (marker) return null;

	let latlng = event.latlng;
	console.log(latlng);
	let msg = `(${latlng.lat.toFixed(2)}, ${latlng.lng.toFixed(2)})`
	marker = L.marker(latlng).addTo(mp);
	$('.leaflet-container').css('cursor','');
	$('#userLoc').val(msg);
}
function mouseEnter(event) {
	if (marker) {
		$('.leaflet-container').css('cursor','');
	} else {
		$('.leaflet-container').css('cursor','crosshair');
		// console.log(event);
	}
}

$('#startOver').click(() => {
	mp.removeLayer(marker);
	marker = null;
});

