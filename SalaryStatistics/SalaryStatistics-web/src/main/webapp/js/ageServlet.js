var showAgeDataInGraph = function (data) {
    var div = $('<div id="graph-data0" style="width:100%; height:400px;"></div>');
    $("#graphs").append(div);
    var allIntervals = [];
    $.each(data, function (i, item) {
        allIntervals.push(makeInterval(data[i]));
    });
    var intervals = [];
    $.each(allIntervals, function (i, el) {
        if ($.inArray(el, intervals) === -1)
            intervals.push(el);
    });
    var years = makeSet(data, "year");
    var countries = makeSet(data, "country");
    var sexes = makeSet(data, "sex");

    years.sort();
    countries.sort();
    sexes.sort();
    //intervals.sort();

    var xAxis = [];
    for (var yearIndex in years) {
        for (var intervalIndex in intervals) {
            for (var sexesIndex in sexes) {
                xAxis.push(years[yearIndex] + " " + intervals[intervalIndex] + " " + sexes[sexesIndex] /*getIntervalName(data, intervals[intervalIndex])*/);
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
            text: 'Platy dle věku'
        },
        xAxis: {
            categories: xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: 'Plat [CZK]'
            }
        },
        series: series
    });
};

var showAgeDataBySex = function (data) {
    var allIntervals = [];
    $.each(data, function (i, item) {
        allIntervals.push(makeInterval(data[i]));
    });
    var intervals = [];
    $.each(allIntervals, function (i, el) {
        if ($.inArray(el, intervals) === -1)
            intervals.push(el);
    });
    var years = makeSet(data, "year");
    var countries = makeSet(data, "country");
    var sexes = makeSet(data, "sex");

    years.sort();
    countries.sort();
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
            for (var intervalIndex in intervals) {
                xAxis.push(years[yearIndex] + " " + intervals[intervalIndex]);
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
            filteredBySex.sort(compare);
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
                text: 'Platy dle veku a pohlavi ' + country
            },
            xAxis: {
                categories: xAxis
            },
            series: series
        });
    }
};

var showAgeDataByYearAndSex = function (data) {
    var allIntervals = [];
    $.each(data, function (i, item) {
        allIntervals.push(makeInterval(data[i]));
    });
    var intervals = [];
    $.each(allIntervals, function (i, el) {
        if ($.inArray(el, intervals) === -1)
            intervals.push(el);
    });
    var years = makeSet(data, "year");
    var countries = makeSet(data, "country");
    var sexes = makeSet(data, "sex");
    years.sort();
    countries.sort();
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
            for (var i in intervals) {
                var interval = intervals[i];
                series.push({name: interval, data: []});
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
                var filteredByInterval = [];
                for (var index in filteredByCountryAndSex) {
                    var entry = filteredByCountryAndSex[index];
                    if (makeInterval(entry) === serie.name) {
                        filteredByInterval.push(entry);
                    }
                }
                filteredByInterval.sort(compare);
                for (var index in filteredByInterval) {
                    var entry = filteredByInterval[index];
                    serie.data.push(entry.averageSalary);
                }
            }
            var div = $('<div id="graph-data' + (((c + 1) * (s + 1)) + countries.length + 1) + '" style="width:100%; height:400px;"></div>');
            $("#graphs").append(div);

            $("#graph-data" + (((c + 1) * (s + 1)) + countries.length + 1)).highcharts({
                title: {
                    text: 'Rust platu (' + sex + ') dle veku ' + country
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

var compare = function (a, b) {
    if (a.year > b.year)
        return -1;
    if (a.year < b.year)
        return 1;
    if (a.ageFrom < b.ageFrom) {
        return -1;
    }
    if (a.ageFrom > b.ageFrom) {
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

function makeInterval(entry) {
    var interval = "";
    if (entry.ageFrom == "0") {
        interval += "Do-" + entry.ageTo;
    } else if (entry.ageTo == "100") {
        interval += entry.ageFrom + "-a více";
    } else {
        interval += entry.ageFrom + "-" + entry.ageTo;
    }
    return interval;
};