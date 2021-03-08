
var show_name = hash.get("name");

if(show_name != "" && show_name != undefined){
    getShow(show_name);
}

$("#search").click(function () {
    var show_name = $("#show_name").val();
    getShow(show_name);
    hash.add({name:show_name});
});

$("#main_table button").live("click", function () {

    var show_id = $(this).attr("data-value");
    if(show_id != "" && show_id != undefined){
        offline(show_id);
    }

});

function offline(show_id) {

    $.ajax({
        type: 'GET',
        url: '/show/offline?showId=' + show_id,
        success: function (data) {
            if("SUCCESS" == data){
                window.location.reload();
            }
        }
    });
    
}

function getShow(name) {
    $.ajax({
        type:'GET',
        url:'/show/list?name=' + name,
        success:function(data){

            var html = "<tr><td> 名称 </td><td> 平台 </td> <td>标记删除</td> </tr>";

            $.each(data, function (i, o) {

                var del_html = "";

                if(o.deleted){
                    del_html = "<button class='btn btn-danger'>已删除</button>";
                }else {
                    del_html = "<button class='btn btn-default' data-value='" + o.id + "'>点击删除</button>";
                }

                html += "<tr> <td><a href='"+o.url+"' target='_blank'>"+o.name+"</a></td> <td>" + o.platform_name + "</td> <td>"+del_html+"</td> </tr>";
                
            });

            $("#main_table").html(html);
            
        }

    });
}