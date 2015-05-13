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


    var showSectorDataInGraph = function (data) {
        var yearsSet = {};
        var namesSet = {};
        var series = [];
        for (var index in data) {
            var entry = data[index];
            yearsSet[entry.year] = true;
            if (!(entry.name in namesSet)) {
                namesSet[entry.name] = [];
            }
            namesSet[entry.name].push(entry);
        }
        var years = [];
        for (var index in yearsSet) {
            years.push(index);
        }
        for (var index in namesSet) {
            var category = namesSet[index];
            var serie = {
                name: "",
                data: []
            };
            var compare = function (a, b) {
                if (a.year < b.year)
                    return -1;
                if (a.year > b.year)
                    return 1;
                return 0;
            };

            category.sort(compare);
            for (var entryIndex in category) {
                serie.name = category[entryIndex].name;
                serie.data.push(category[entryIndex].averageSalary);
            }
            series.push(serie);
        }
        $("#graph-data").highcharts({
            chart: {
                type: 'column'
            },
            title: {
                text: 'Plat v odvetvich'
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