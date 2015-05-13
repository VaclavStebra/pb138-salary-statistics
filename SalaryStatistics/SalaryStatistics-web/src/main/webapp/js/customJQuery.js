$(document).ready(function () {

    var ajaxSubmitForm = function () {
        $("[data-ajax-form]").submit(function (e) {
            $("#table-data").html("");
            $("#graph-data").html("");
            e.preventDefault();
            var url = $(this).attr("action");
            $.get(url + "/tabledata?" + $(this).serialize(), function (data) {
                $("#table-data").html(data);
            });
            $.get(url + "/data?" + $(this).serialize(), function (data) {
                showSectorDataInGraph(data);
            });
        });
    };

    var makeSet = function(data, property) {
        var set = [];
        for (var index in data) {
            var entry = data[index];
            set[entry[property]] = true;
        }
        var result = [];
        for (var item in set) {
            result.push(item);
        }
        return result;
    };
    
    var getSectorName = function(data, code) {
        for (var i in data) {
            var entry = data[i];
            if (entry.code === code) {
                return entry.name;
            }
        }
        return null;
    };

    var showSectorDataInGraph = function (data) {
        var div = $('<div id="graph-data1" style="width:100%; height:400px;"></div>');
        $("#graphs").append(div);
        var years = makeSet(data, "year");
        var countries = makeSet(data, "country");
        var sectors = makeSet(data, "code");
        years.sort();
        countries.sort();
        sectors.sort();
        var xAxis = [];
        for (var yearIndex in years) {
            for (var sectorIndex in sectors) {
                xAxis.push(years[yearIndex] + " " + getSectorName(data, sectors[sectorIndex]));
            }
        }
        var series = [];
        for (var index in countries) {
            series.push({name: countries[index], data: []});
        }
        for (var index in series) {
            var countryName = series[index].name;
            var salariesByCountry = [];
           
            for (var i in data) {
                var entry = data[i];
                if (entry.country === countryName) {
                    salariesByCountry.push(entry);
                }
            }
            
            var compare = function (a, b) {
                if (a.year < b.year)
                    return -1;
                if (a.year > b.year)
                    return 1;
                if (a.code < b.code) {
                    return -1;
                }
                if (a.code > b.code) {
                    return 1;
                }
                return 0;
            };

            salariesByCountry.sort(compare);
            for (var i in salariesByCountry) {
                series[index].data.push(salariesByCountry[i].averageSalary);
            }
            
        } 
       
        $("#graph-data1").highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: 'Plat v odvetvich'
            },
            xAxis: {
                categories: xAxis
            },
            yAxis: {
                title: {
                    text: 'Plat [CZK]'
                }
            },
            series: series
        });
    };
    ajaxSubmitForm();
    var url = $("[data-category-sector]").data("category-sector");
    $.get(url + "/data", function (data) {
        showSectorDataInGraph(data);
    });
    /*$(".dropdown li a").click(function(){
     $("#first-select").html($(this).text() + " <span class=\"caret\"></span>");
     });*/
    $("[data-category-sector]").click(function () {
        $("#options").html("");
        $("#table-data").html("");
        $("#graph-data").html("");
        var url = $(this).data("category-sector");
        $.get(url + "/options", function (data) {
            $("#options").html(data);
            ajaxSubmitForm();
        });
    });
});