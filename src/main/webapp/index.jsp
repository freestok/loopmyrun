<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<html>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="identifier-url" content="https://loopmyrun.herokuapp.com" />
<meta name="title" content="LoopMyRun" />
<meta name="description" content="A tool to help you build loops for running, walking, or biking!" />
<meta name="abstract" content="A tool to help you build loops for running, walking, or biking!" />
<meta name="keywords" content="running, loops, walk, bike" />
<meta name="author" content="Kray Freestone" />
<meta name="language" content="EN" />


<!-- STYLESHEETS -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" integrity="sha512-xodZBNTC5n17Xt2atTPuE1HxjVMSvLVW9ocqUKLsCC5CXdbqCmblAshOMAS6/keqq/sMZMZ19scR4PsZChSR7A==" crossorigin=""/>
<link rel="stylesheet" href="css/style.css">
<link rel="stylesheet" href="https://unpkg.com/leaflet-geosearch@3.1.0/dist/geosearch.css"/>
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">

<!-- JS imports -->
<script src="https://code.jquery.com/jquery-3.2.1.min.js" ></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
<script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js" integrity="sha512-XQoYMqMTK8LvdxXYG3nZ448hOEQiglfqkJs1NOQV44cWnUrBc8PkAOcXy20w0vlaXaVUearIOBhiXZ5V3ynxwA==" crossorigin=""></script>
<script src="https://unpkg.com/leaflet-geosearch@3.1.0/dist/geosearch.umd.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@turf/turf@5/turf.min.js"></script>

<body>
    <nav class="navbar navbar-dark bg-dark">
        <div class="navbar-brand mx-auto">LoopMyRun
              <a target="_blank" href="https://github.com/freestok/loopmyrun"><i class="fa fa-github"></i></a>
        </div>

      </nav>
    <div class="container-fluid h-100">
        <!-- Image and text -->

        <div class="row h-100">
            <div class="col-md-3">
                <ul class="nav nav-pills nav-fill">
                    <li class="nav-item">
                      <a id="formTab" class="nav-link active" href="#">Loop Finder</a>
                    </li>
                    <li class="nav-item">
                      <a id="resultsTab" class="nav-link" href="#">Loop Results</a>
                    </li>
                </ul>
                
                <!-- Loop Finder Tab -->
                <form id="loopForm">
                    <div class="form-group">
                        <label for="userLoc" class="font-weight-bold">Enter a Location</label>
                        <input type="text" class="form-control" id="userLoc" aria-describedby="userLocation"
                            placeholder="Drop a point or search for an address" required disabled>
                    </div>
                    <div class="form-group">
                        <label for="userDistance" class="font-weight-bold">Distance</label>
                        <input type="number" class="form-control" id="userDistance" min="1" placeholder="Max Dist. of 20 mi." required>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="distanceUnit" id="mile" value="mile" required>
                        <label class="form-check-label" for="inlineRadio1">Miles</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="distanceUnit" id="km" value="km" required>
                        <label class="form-check-label" for="inlineRadio2">Kilometers</label>
                    </div>
                    <div>
                        <button type="submit" class="btn btn-primary">Find a Loop!</button>
                        <button type="reset" id="startOver" class="btn">Start Over</button>
                    </div>
                </form>

                <!-- Loop Results tab -->
                <div id="loopResults"></div>

            </div>
            <div id="leaflet-container" class="col-md-9 p-0">
                <div class="custom-popup" id="map"></div>
            </div>
        </div>
    </div>

    <script src="js/loopMap.js"></script>
</body>
</html>
