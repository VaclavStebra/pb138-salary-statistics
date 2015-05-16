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

            var div = $('<div id="graph-data' + (c + 2) + '" style="width:100%; height:400px;"></div>');
            $("#graphs").append(div);

            $("#graph-data" + (c + 2)).highcharts({
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

    var showEducationDataInGraph = function (data) {
        var div = $('<div id="graph-data0" style="width:100%; height:400px;"></div>');
        $("#graphs").append(div);
        var years = makeSet(data, "year");
        var countries = makeSet(data, "country");
        var degrees = makeSet(data, "degree");
        var sexes = makeSet(data, "sex");
        years.sort();
        countries.sort();
        degrees.sort();
        sexes.sort();
        var xAxis = [];
        for (var yearIndex in years) {
            for (var degreeIndex in degrees) {
                for (var sexesIndex in sexes) {
                    xAxis.push(years[yearIndex] + " " + degrees[degreeIndex] +
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
                text: 'Plat v odvetvich'
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

    var showEducationDataBySex = function (data) {
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

            var xAxis = [];
            for (var yearIndex in years) {
                for (var degreeIndex in degrees) {
                    xAxis.push(years[yearIndex] + " " + degrees[degreeIndex]);
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
                    return a.degree > b.degree;
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
                    text: 'Platy dle vzdelani a pohlavi ' + country
                },
                xAxis: {
                    categories: xAxis
                },
                series: series
            });
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
                    for (var index in filteredByDegree) {
                        var entry = filteredByDegree[index];
                        serie.data.push(entry.averageSalary);
                    }
                }

                var div = $('<div id="graph-data' + (((c + 1) * (s + 1)) + countries.length + 1) + '" style="width:100%; height:400px;"></div>');
                $("#graphs").append(div);

                $("#graph-data" + (((c + 1) * (s + 1)) + countries.length + 1)).highcharts({
                    title: {
                        text: 'Rust platu (' + sex + ') dle vzdelani ' + country
                    },
                    xAxis: {
                        categories: years
                    },
                    series: series
                });
            }
        }

    };

    var url = $("[data-graph-url]").data("graph-url");
    $.get(url + "/data?" + $("#options form").serialize(), function (data) {
        switch (url) {
            case "sector":
                showSectorDataInGraph(data);
                showSectorDataByCountry(data);
                break;
            case "education":
                showEducationDataInGraph(data);
                showEducationDataBySex(data);
                showEducationDataByYearAndSex(data);
                break;
        }
    });

});