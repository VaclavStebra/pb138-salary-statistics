$(document).ready(function () {

    var makeSet = function (data, property) {
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

    var getSectorName = function (data, code) {
        for (var i in data) {
            var entry = data[i];
            if (entry.code === code) {
                return entry.name;
            }
        }
        return null;
    };

    var showSectorDataByCountry = function (data) {

        var years = makeSet(data, "year");
        var codes = makeSet(data, "code");
        var countries = makeSet(data, "country");
        years.sort();
        codes.sort();

        for (var c in countries) {
            var country = countries[c];
            var filteredData = [];
            for (var i in data) {
                var entry = data[i];
                if (entry.country === country) {
                    filteredData.push(entry);
                }
            }
            var series = [];
            for (var i in codes) {
                var code = codes[i];
                series.push({name: code, data: []});
            }

            for (var i in series) {
                var serie = series[i];
                var filteredByCode = [];
                for (var index in filteredData) {
                    var entry = filteredData[index];
                    if (entry.code === serie.name) {
                        filteredByCode.push(entry);
                    }
                }
                filteredByCode.sort(function (a, b) {
                    return a.year - b.year;
                });
                for (var index in filteredByCode) {
                    var entry = filteredByCode[index];
                    serie.data.push(entry.averageSalary);
                }
                serie.name = getSectorName(data, serie.name);
            }

            var div = $('<div id="graph-data' + (c+2) + '" style="width:100%; height:400px;"></div>');
            $("#graphs").append(div);

            $("#graph-data" + (c+2)).highcharts({
                title: {
                    text: 'Rust platu dle odvetvi v ' + country
                },
                xAxis: {
                    categories: years
                },
                series: series
            });
        }
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
    $.get("sector/data?" + $("#options form").serialize(), function (data) {
        showSectorDataInGraph(data);
        showSectorDataByCountry(data);
    });
});