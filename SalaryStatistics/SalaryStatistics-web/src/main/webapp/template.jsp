<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="cs-CZ">
    <head>
        <meta charset="utf-8">
        <title>Statistiky platů</title>
        <link rel="stylesheet" type="text/css" href="css/bootstrap.css" />
        <link rel="stylesheet" type="text/css" href="css/custom.css" />
        <script src="js/jquery-2.1.4.min.js"></script>
        <script src="js/bootstrap.js"></script>
        <script src="js/highcharts.js"></script>
        <script src="js/ageServlet.js"></script>
        <script src="js/regionServlet.js"></script>
        <script src="js/sectorServlet.js"></script>
        <script src="js/educationServlet.js"></script>
        <script src="js/classificationServlet.js"></script>
        <script src="js/customJQuery.js"></script>
    </head>
    <body>
        <div class="header-holder">
            <h1 class="header container">Statistika platů</h1>
        </div>
        <div class="container">
            <nav class="navbar navbar-inverse">
                <div class="container navbar-header">
                    <a class="navbar-brand" href="#">Hlavní rozdelení</a>
                </div>
                <div class="navbar-collapse" id="bs-example-navbar-collapse-1">
                    <ul class="nav navbar-nav">
                        <li class="dropdown">
                            <a href="#" id="first-select" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Výber <span class="caret"></span></a>
                            <ul class="dropdown-menu" role="menu">
                                <li><a href="${pageContext.request.contextPath}/sector">Sektor</a></li>
                                <li><a href="${pageContext.request.contextPath}/age">Vek</a></li>
                                <li><a href="${pageContext.request.contextPath}/education">Vzdelání</a></li>
                                <li><a href="${pageContext.request.contextPath}/region">Región</a></li>
                                <li><a href="${pageContext.request.contextPath}/classification">Klasifikace</a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </nav>
            <h2><c:out value="${heading}" /></h2>
            <div id="options">
                <c:out value="${options}" escapeXml="false" />
            </div>
            <div id="table-data">
                <c:out value="${table}" escapeXml="false" />
            </div>
            <div id="graphs" data-graph-url="${graphUrl}">

            </div>
        </div>

    </body>
</html>
