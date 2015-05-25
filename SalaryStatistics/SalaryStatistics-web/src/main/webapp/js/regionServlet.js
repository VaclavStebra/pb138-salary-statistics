var showRegionDataInGraph = function (data) {
    var countries = makeSet(data, "country");
    if (countries.length === 1) {
        return;
    }
    var years = makeSet(data, "year");
    var names = makeSet(data, "name");
    var sexes = makeSet(data, "sex");
    years.sort();
    countries.sort();
    names.sort();
    sexes.sort();
    for (var s in sexes) {
        var sex = sexes[s];
        for (var n in names) {
            var name = names[n];
            var series = [];
            for (var index in countries) {
                series.push({name: countries[index], data: []});
            }
            for (var index in series) {
                var countryName = series[index].name;
                var salariesByCountryAndName = [];

                for (var i in data) {
                    var entry = data[i];
                    if (entry.country === countryName && entry.sex === sex && entry.name === name) {
                        salariesByCountryAndName.push(entry);
                    } else if(entry.country === countryName && entry.sex === undefined && sex === "undefined"
                            && entry.name === name) {
                        salariesByCountryAndName.push(entry);
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

                salariesByCountryAndName.sort(compare);
                for (var i in salariesByCountryAndName) {
                    series[index].data.push(salariesByCountryAndName[i].averageSalary);
                }

            }
            var div = $('<div id="graph-data-degrees' + n + s + '" style="width:100%; height:400px;"></div>');
            $("#graphs").append(div);
            $("#graph-data-degrees" + n + s).highcharts({
                chart: {
                    type: 'bar'
                },
                title: {
                    text: 'Plat (' + ((sex !== 'undefined') ? sex: "spolu") + ') ' + name
                },
                xAxis: {
                    categories: years
                },
                yAxis: {
                    min: 300,
                    title: {
                        text: 'Plat [CZK]'
                    }
                },
                series: series
            });
        }
    }
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
        for (var n in names) {
            var name = names[n];
            var country = countries[c];
            var filteredData = [];
            for (var i in data) {
                var entry = data[i];
                if (entry.country === country && entry.name === name) {
                    filteredData.push(entry);
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
                    } else if (entry.sex === undefined && serie.name === 'undefined') {
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
                for (var y in years) {
                    var year = years[y];
                    var found = false;
                    for (var i in filteredBySex) {
                        var entry = filteredBySex[i];
                        if (entry.year === year) {
                            serie.data.push(entry.averageSalary);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        serie.data.push(null);
                    }
                }
                if (serie.name === 'undefined') {
                    serie.name = 'spolu';
                }

            }

            var div = $('<div id="graph-data' + c + n + '" style="width:100%; height:400px;"></div>');
            $("#graphs").append(div);

            $("#graph-data" + c + n).highcharts({
                chart: {
                    type: 'bar'
                },
                title: {
                    text: 'Platy ' + name + ' v ' + country
                },
                xAxis: {
                    categories: years
                },
                series: series
            });
        }
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
                for (var y in years) {
                    var year = years[y];
                    var found = false;
                    for (var i in filteredByName) {
                        var entry = filteredByName[i];
                        if (entry.year === year) {
                            serie.data.push(entry.averageSalary);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        serie.data.push(null);
                    }
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