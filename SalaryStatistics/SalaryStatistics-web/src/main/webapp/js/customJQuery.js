$(document).ready(function () {

    var url = $("[data-graph-url]").data("graph-url");
    $.get(url + "/data?" + $("#options form").serialize(), function (data) {
        if($(".message").length > 0) {
            return;
        }
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
            case "region":
                showRegionDataBySex(data);
                showRegionDataByYearAndSex(data);
                showRegionDataInGraph(data);
                break;
            case "age":
                showAgeDataBySex(data);
                showAgeDataByYearAndSex(data);
                showAgeDataInGraph(data);
                break;
            case "classification":
                showClassificationDataByCountry(data);
                showClassificationDataInGraph(data);
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
