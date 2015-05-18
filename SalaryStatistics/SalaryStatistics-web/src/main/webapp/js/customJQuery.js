$(document).ready(function () {

    var url = $("[data-graph-url]").data("graph-url");
    $.get(url + "/data?" + $("#options form").serialize(), function (data) {
        switch (url) {
            case "sector":
                showSectorDataByCountry(data);
                showSectorDataInGraph(data);
                break;
            case "education":
                showEducationDataBySex(data);
                showEducationDataByYearAndSex(data);
                showEducationDataInGraph(data);
                break;
            case "age":
                showAgeDataInGraph(data);
                showAgeDataBySex(data);
                showAgeDataByYearAndSex(data);
                break;
        }
    });
    
    $("[data-show-nothing-options]").click(function(e) {
        e.preventDefault();
        var name = $(this).data("show-nothing-options");
        $("[name=" + name + "]").removeAttr("checked");
    });
    $("[data-show-all-options]").click(function(e) {
        e.preventDefault();
        var name = $(this).data("show-all-options");
        $("[name=" + name + "]").prop("checked", "checked");
    });

});