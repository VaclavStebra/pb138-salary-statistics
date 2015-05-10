$(document).ready(function () {
    /*$(".dropdown li a").click(function(){
     $("#first-select").html($(this).text() + " <span class=\"caret\"></span>");
     });*/
    $("[data-category-sector]").click(function () {
        var url = $(this).data("category-sector");
        $.get(url + "/tabledata", function(data) {
            $("#table-data").html(data);
        });
        $.get(url + "/data", function(data) {
            showSectorDataInGraph(data)
        });
    });
    var showSectorDataInGraph = function (data) {
        var categories = [];
        var seriesValues = [];
        for (var index in data) {
            var entry = data[index];
            categories.push(entry.name);
            seriesValues.push(entry.averageSalary);
        }
        var series = {
            name: "",
            data: seriesValues
        };
        $("#graph-data").highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: 'Plat v odvetvich'
            },
            xAxis: {
                categories: categories
            },
            yAxis: {
                title: {
                    text: 'Plat [CZK]'
                }
            },
            series: [{
                    name: 'Plat',
                    data: seriesValues
                }]
        });
    }
});