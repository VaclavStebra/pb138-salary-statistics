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

var showEducationDataInGraph = function (data) {
    var years = makeSet(data, "year");
    var countries = makeSet(data, "country");
    var degrees = makeSet(data, "degree");
    var sexes = makeSet(data, "sex");
    if (countries.length === 1) {
        return;
    }
    years.sort();
    countries.sort();
    degrees.sort();
    sexes.sort();
    for (var s in sexes) {
        var sex = sexes[s];
        for (var d in degrees) {
            var degree = degrees[d];

            var series = [];
            for (var index in countries) {
                series.push({name: countries[index], data: []});
            }
            for (var index in series) {
                var countryName = series[index].name;
                var salariesByCountryAndDegree = [];

                for (var i in data) {
                    var entry = data[i];
                    if (entry.country === countryName && entry.sex === sex && entry.degree === degree) {
                        salariesByCountryAndDegree.push(entry);
                    } else if (entry.country === countryName && entry.sex === undefined && sex === "undefined"
                            && entry.degree === degree) {
                        salariesByCountryAndDegree.push(entry);
                    }
                }

                var compare = function (a, b) {
                    if (a.year < b.year)
                        return -1;
                    if (a.year > b.year)
                        return 1;
                    if (a.degree < b.degree) {
                        return -1;
                    }
                    if (a.degree > b.degree) {
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

                salariesByCountryAndDegree.sort(compare);
                for (var i in salariesByCountryAndDegree) {
                    series[index].data.push(salariesByCountryAndDegree[i].averageSalary);
                }

            }
            var div = $('<div id="graph-data-degrees' + d + s + '" style="width:100%; height:400px;"></div>');
            $("#graphs").append(div);
            $("#graph-data-degrees" + d + s).highcharts({
                chart: {
                    type: 'bar'
                },
                title: {
                    text: 'Plat (' + ((sex !== 'undefined') ? sex : "spolu") + ') ' + degree
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

var showEducationDataBySex = function (data) {
    var years = makeSet(data, "year");
    var degrees = makeSet(data, "degree");
    var countries = makeSet(data, "country");
    var sexes = makeSet(data, "sex");
    years.sort();
    degrees.sort();
    sexes.sort();

    for (var c in countries) {
        for (var d in degrees) {
            var degree = degrees[d];
            var country = countries[c];
            var filteredData = [];
            for (var i in data) {
                var entry = data[i];
                if (entry.country === country && entry.degree === degree) {
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
                    return a.degree > b.degree;
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

            var div = $('<div id="graph-data-by-sex' + c + d + '" style="width:100%; height:400px;"></div>');
            $("#graphs").append(div);

            $("#graph-data-by-sex" + c + d).highcharts({
                chart: {
                    type: 'bar'
                },
                title: {
                    text: 'Platy ' + degree + ' v ' + country
                },
                xAxis: {
                    categories: years
                },
                series: series
            });
        }
    }
};

var showEducationDataByYearAndSex = function (data) {
    var years = makeSet(data, "year");
    var degrees = makeSet(data, "degree");
    var countries = makeSet(data, "country");
    var sexes = makeSet(data, "sex");
    years.sort();
    degrees.sort();
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
            for (var i in degrees) {
                var degree = degrees[i];
                series.push({name: degree, data: []});
            }

            var filteredByCountryAndSex = [];
            for (var i in filteredData) {
                var entry = filteredData[i];
                if (entry.sex === sex) {
                    filteredByCountryAndSex.push(entry);
                } else if (entry.sex === undefined && sex === "undefined") {
                    filteredByCountryAndSex.push(entry);
                }
            }

            for (var i in series) {
                var serie = series[i];
                var filteredByDegree = [];
                for (var index in filteredByCountryAndSex) {
                    var entry = filteredByCountryAndSex[index];
                    if (entry.degree === serie.name) {
                        filteredByDegree.push(entry);
                    }
                }
                filteredByDegree.sort(function (a, b) {
                    var result = a.year - b.year;
                    if (result !== 0) {
                        return result;
                    }
                    return a.degree > b.degree;
                });

                for (var y in years) {
                    var year = years[y];
                    var found = false;
                    for (var i in filteredByDegree) {
                        var entry = filteredByDegree[i];
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
                    text: 'Rust platu (' + ((sex !== "undefined") ? sex : "spolu") + ') dle vzdelani ' + country
                },
                xAxis: {
                    categories: years
                },
                series: series
            });
        }
    }

};

