$("input[name='showName']").live("blur", function () {
    var name = $(this).val();
    myFunction(name);
});

$(".down").live("click",function () {
    var id = $(this).attr("data-value");
    if(id == undefined || id == "undefined" || id.trim() == ""){
        alert("无此剧Id");
        return;
    }

    $.ajax({
        type:"GET",
        url:"/show/downShow?showId=" + id,
        dataType: "json",
        success:function (data) {
            var name = data.name;
            myFunction(name)
        }
    })
});

function myFunction(name) {
    if (name != null && name != undefined && name != "") {
        $.ajax({
            type: "GET",
            url: "/show/status?showName=" + name,
            dataType: "json",
            success: function (data) {
                var html = "";
                $(data).each(function (i, o) {
                    var bh = "";
                    $(o.jobs).each(function (i, job) {
                        bh += "<tr><td colspan='2' style='text-align: center'>抓取频率："+job.frequency+"</td><td colspan='10'><a href='"+job.url+"' target='_blank'>"+job.url.substring(0,80)+"</a></td></tr>"
                    });
                    html += "<tr><td style='text-align: center'>" + o.id + "</td><td style='text-align: center'>" + o.name + "</td><td style='text-align: center'>" + o.code + "</td><td style='text-align: center'>" + o.parent_id + "</td><td style='text-align: center'>" + o.depth + "</td>" +
                        "<td style='text-align: center'>" + o.deleted + "</td><td style='text-align: center'>" + o.platform_id + "</td><td style='text-align: center'>" + o.category + "</td><td style='text-align: center'>" + o.linked_id + "</td><td style='text-align: center'>" + (Number(o.on_billboard)==0?"否":"是")+ "</td>" +
                        "<td style='text-align: center'>" + o.checked_status + "</td><td style='text-align: center'><a class='down' data-value='"+o.id+"' href='javascript:void(0);'>下架</a></td></tr>"+bh;
                });
                $("#showTableBody").html($(html));
            }
        });
    }
}