$("input[name='actorName']").live("blur", function () {
    var name = $(this).val();
    var $th = $(this);
    if (name != null && name != undefined && name != "") {
        $.ajax({
            type: "GET",
            url: "/douban/guessactor?name=" + name,
            dataType: "json",
            success: function (data) {

                if (typeof(data.msg) == "undefined") {
                    console.log(data);
                    var html = "";
                    $(data).each(function (i, o) {
                        html += "<a href='javascript:void(0);'  onclick='searchActor(this)' aid = '+" + o.id + "' aname = '" + o.name + "' >" + '|' + o.name + '|' + "   </a>";
                    });
                    $("#matching_select").html(html);
                    $("#matching_select").show();
                } else {

                }

            }
        });
    }

});

function searchActor(node) {
    var name = $(node).attr("aname");
    var actorId = $(node).attr("aid");
    $("#actorName").val(name);
    $("#showTableBody").html("");
    $.ajax({
        type: "GET",
        url: "/douban/actorplayed?id=" + actorId,
        dataType: "json",
        success: function (data) {

            if (typeof(data.msg) == "undefined") {
                console.log(data);
                var html = "";
                $(data).each(function (i, o) {
                    html += "<tr><td>" + o.id + "</td><td>" + o.name + "</td><td>" + o.url + "</td></tr>";
                });
                $("#showTableBody").append($(html));
            } else {

            }

        }
    });
}