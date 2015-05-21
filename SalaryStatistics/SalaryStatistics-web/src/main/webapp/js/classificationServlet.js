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

var getClassificationName = function (data, code) {
    for (var i in data) {
        var entry = data[i];
        if (entry.code === code) {
            return entry.name;
        }
    }
    return null;
};

var showClassificationDataByCountry = function (data) {

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
            
            for (var y in years) {
                var year = years[y];
                var found = false;
                for (var index in filteredByCode) {
                    var entry = filteredByCode[index];
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
            serie.name = getClassificationName(data, serie.name);
        }

        var div = $('<div id="graph-data-classification' + c + '" style="width:100%; height:400px;"></div>');
        $("#graphs").append(div);

        $("#graph-data-classification" + c).highcharts({
            title: {
                text: 'Rust platu dle klasifikace v ' + country
            },
            xAxis: {
                categories: years
            },
            series: series
        });
    }
};

var showClassificationDataInGraph = function (data) {
    var years = makeSet(data, "year");
    var countries = makeSet(data, "country");
    var codes = makeSet(data, "code");
    years.sort();
    countries.sort();
    codes.sort();
    if (countries.length === 1) {
        return;
    }
    for (var c in codes) {
        var classification = codes[c];
        var series = [];
        for (var index in countries) {
            series.push({name: countries[index], data: []});
        }
        for (var index in series) {
            var countryName = series[index].name;
            var salariesByCountryAndCode = [];

            for (var i in data) {
                var entry = data[i];
                if (entry.country === countryName && entry.code === classification) {
                    salariesByCountryAndCode.push(entry);
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

            salariesByCountryAndCode.sort(compare);
            for (var y in years) {
                var year = years[y];
                var found = false;
                for (var i in salariesByCountryAndCode) {
                    var entry = salariesByCountryAndCode[i];
                    if (entry.year === year) {
                        series[index].data.push(entry.averageSalary);
                        found = true;
                        break;
                    }
                }                
                if (!found) {
                    series[index].data.push(null);
                }
            }

        }

        var div = $('<div id="graph-data-classifications-' + classification + '" style="width:100%; height:400px;"></div>');
        $("#graphs").append(div);
        $("#graph-data-classifications-" + classification).highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: 'Porovnani platu v klasifikace ' + getClassificationName(data, classification) + ' dle zemi'
            },
            xAxis: {
                categories: years
            },
            yAxis: {
                title: {
                    text: 'Plat [CZK]'
                }
            },
            series: series
        });
    }
};



