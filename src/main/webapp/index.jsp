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

<link rel="stylesheet" href="css/style.css">
<%--leaflet--%>
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css"
      integrity="sha512-xodZBNTC5n17Xt2atTPuE1HxjVMSvLVW9ocqUKLsCC5CXdbqCmblAshOMAS6/keqq/sMZMZ19scR4PsZChSR7A=="
      crossorigin=""/>

<script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"
        integrity="sha512-XQoYMqMTK8LvdxXYG3nZ448hOEQiglfqkJs1NOQV44cWnUrBc8PkAOcXy20w0vlaXaVUearIOBhiXZ5V3ynxwA=="
        crossorigin=""></script>
<body>
<div id="map"></div>

<script src="js/loopMap.js"></script>
</body>
</html>