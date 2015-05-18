var showRegionDataInGraph = function (data) {
    var div = $('<div id="graph-data0" style="width:100%; height:400px;"></div>');
    $("#graphs").append(div);
    var years = makeSet(data, "year");
    var countries = makeSet(data, "country");
    var names = makeSet(data, "name");
    var sexes = makeSet(data, "sex");
    years.sort();
    countries.sort();
    names.sort();
    sexes.sort();
    var xAxis = [];
    for (var yearIndex in years) {
        for (var nameIndex in names) {
            for (var sexesIndex in sexes) {
                xAxis.push(years[yearIndex] + " " + names[nameIndex] +
                        " " + sexes[sexesIndex]);
            }
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
            if (a.name < b.name) {
                return -1;
            }
            if (a.name > b.name) {
                return 1;
            }
            if (a.sex < b.sex) {
                return -1;
            }
            if (a.sex > b.sex) {
                return 1;
            }
            return 0;
        };

        salariesByCountry.sort(compare);
        for (var i in salariesByCountry) {
            series[index].data.push(salariesByCountry[i].averageSalary);
        }

    }

    $("#graph-data0").highcharts({
        chart: {
            type: 'bar'
        },
        title: {
            text: 'Plat v regionech'
        },
        xAxis: {
            categories: xAxis
        },
        yAxis: {
            min: 300,
            title: {
                text: 'Plat [CZK]'
            }
        },
        series: series
    });
};

var showRegionDataBySex = function (data) {
    var years = makeSet(data, "year");
    var names = makeSet(data, "name");
    var countries = makeSet(data, "country");
    var sexes = makeSet(data, "sex");
    years.sort();
    names.sort();
    sexes.sort();

    for (var c in countries) {
        var country = countries[c];
        var filteredData = [];
        for (var i in data) {
            var entry = data[i];
            if (entry.country === country) {
                filteredData.push(entry);
            }
        }

        var xAxis = [];
        for (var yearIndex in years) {
            for (var nameIndex in names) {
                xAxis.push(years[yearIndex] + " " + names[nameIndex]);
            }
        }

        var series = [];
        for (var i in sexes) {
            var sex = sexes[i];
            series.push({name: sex, data: []});
        }

        for (var i in series) {
            var serie = series[i];
            var filteredBySex = [];
            for (var index in filteredData) {
                var entry = filteredData[index];
                if (entry.sex === serie.name) {
                    filteredBySex.push(entry);
                }
            }
            filteredBySex.sort(function (a, b) {
                var result = a.year - b.year;
                if (result !== 0) {
                    return result;
                }
                return a.name > b.name;
            });
            for (var index in filteredBySex) {
                var entry = filteredBySex[index];
                serie.data.push(entry.averageSalary);
            }
        }

        var div = $('<div id="graph-data' + (c + 1) + '" style="width:100%; height:400px;"></div>');
        $("#graphs").append(div);

        $("#graph-data" + (c + 1)).highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: 'Platy dle regionu a pohlavi ' + country
            },
            xAxis: {
                categories: xAxis
            },
            series: series
        });
    }
};

var showRegionDataByYearAndSex = function (data) {
    var years = makeSet(data, "year");
    var names = makeSet(data, "name");
    var countries = makeSet(data, "country");
    var sexes = makeSet(data, "sex");
    years.sort();
    names.sort();
    sexes.sort();

    for (var c in countries) {
        var country = countries[c];
        var filteredData = [];
        for (var i in data) {
            var entry = data[i];
            if (entry.country === country) {
                filteredData.push(entry);
            }
        }
        for (var s in sexes) {
            var sex = sexes[s];

            var series = [];
            for (var i in names) {
                var name = names[i];
                series.push({name: name, data: []});
            }

            var filteredByCountryAndSex = [];
            for (var i in filteredData) {
                var entry = filteredData[i];
                if (entry.sex === sex) {
                    filteredByCountryAndSex.push(entry);
                }
            }

            for (var i in series) {
                var serie = series[i];
                var filteredByName = [];
                for (var index in filteredByCountryAndSex) {
                    var entry = filteredByCountryAndSex[index];
                    if (entry.name === serie.name) {
                        filteredByName.push(entry);
                    }
                }
                filteredByName.sort(function (a, b) {
                    var result = a.year - b.year;
                    if (result !== 0) {
                        return result;
                    }
                    return a.name > b.name;
                });
                for (var index in filteredByName) {
                    var entry = filteredByName[index];
                    serie.data.push(entry.averageSalary);
                }
            }

            var div = $('<div id="graph-data' + (((c + 1) * (s + 1)) + countries.length + 1) + '" style="width:100%; height:400px;"></div>');
            $("#graphs").append(div);

            $("#graph-data" + (((c + 1) * (s + 1)) + countries.length + 1)).highcharts({
                title: {
                    text: 'Rust platu (' + sex + ') dle regionu ' + country
                },
                xAxis: {
                    categories: years
                },
                series: series
            });
        }
    }

};

function makeSet(data, property) {
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
}
;