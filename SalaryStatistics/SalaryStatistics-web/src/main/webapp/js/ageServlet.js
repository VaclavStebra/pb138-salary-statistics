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

var showAgeDataInGraph = function (data) {
    var countries = makeSet(data, "country");
    if (countries.length === 1) {
        return;
    }
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
    var sexes = makeSet(data, "sex");


    years.sort();
    countries.sort();
    sexes.sort();
    intervals.sort(intervalComparator);

    for (var s in sexes) {
        var sex = sexes[s];
        for (var inter in intervals) {
            var interval = intervals[inter];
            
            var series = [];
            for (var index in countries) {
                series.push({name: countries[index], data: []});
            }
            for (var index in series) {
                var countryName = series[index].name;
                var salariesByCountryAndInterval = [];

                for (var i in data) {
                    var entry = data[i];
                    if (entry.country === countryName && entry.sex === sex && makeInterval(entry) === interval) {
                        salariesByCountryAndInterval.push(entry);
                    } else if (entry.country === countryName && entry.sex === undefined && sex === "undefined"
                            && makeInterval(entry) === interval) {
                        salariesByCountryAndInterval.push(entry);
                    }
                }

                salariesByCountryAndInterval.sort(compare);
                for (var i in salariesByCountryAndInterval) {
                    series[index].data.push(salariesByCountryAndInterval[i].averageSalary);
                }
            }
            var div = $('<div id="graph-data-intervals' + inter + s + '" style="width:100%; height:400px;"></div>');
            $("#graphs").append(div);
            $("#graph-data-intervals" + inter + s).highcharts({
                chart: {
                    type: 'bar'
                },
                title: {
                    text: 'Plat (' + ((sex !== 'undefined') ? sex : "spolu") + ') ' + interval
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
    intervals.sort(intervalComparator);

    for (var c in countries) {
        for (var i in intervals) {
            var interval = intervals[i];
            var country = countries[c];
            var filteredData = [];
            for (var d in data) {
                var entry = data[d];
                if (entry.country === country && makeInterval(entry) === interval) {
                    filteredData.push(entry);
                }
            }

            var series = [];
            for (var s in sexes) {
                var sex = sexes[s];
                series.push({name: sex, data: []});
            }

            for (var s in series) {
                var serie = series[s];
                var filteredBySex = [];
                for (var index in filteredData) {
                    var entry = filteredData[index];
                    if (entry.sex === serie.name) {
                        filteredBySex.push(entry);
                    } else if (entry.sex === undefined && serie.name === 'undefined') {
                        filteredBySex.push(entry);
                    }
                }
                filteredBySex.sort(compare);
                for (var index in filteredBySex) {
                    var entry = filteredBySex[index];
                    serie.data.push(entry.averageSalary);
                }
                if (serie.name === 'undefined') {
                    serie.name = 'spolu';
                }
            }

            var div = $('<div id="graph-data' + c + i + '" style="width:100%; height:400px;"></div>');
            $("#graphs").append(div);

            $("#graph-data" + c + i).highcharts({
                chart: {
                    type: 'bar'
                },
                title: {
                    text: 'Platy ' + interval + ' v ' + country
                },
                xAxis: {
                    categories: years
                },
                series: series
            });
        }
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

var compare = function (a, b) {
    if (a.year < b.year)
        return -1;
    if (a.year > b.year)
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
}
;

var intervalComparator = function (one, two) {
    if (one === two) {
        return 0;
    }
    var parts1 = one.split("-");
    var parts2 = two.split("-");
    if (parts1[0] === "Do") {
        return -1;
    }
    if (parts2[0] === "Do") {
        return 1;
    }
    if (one < two) {
        return -1;
    }
    if (one > two) {
        return 1;
    }
};