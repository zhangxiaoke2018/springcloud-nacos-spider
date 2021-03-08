/**
 * Created by yangtao on 16/7/5.
 */
onload = callBackPagination();
function callBackPagination() {
    var showCount = 10;
    var limit = 10;
    $.ajax({
        type:'GET',
        url:'/show/count?depth=1',
        success:function(totalCount){
            createTable(1, limit, totalCount);
            $('#callBackPager').extendPagination({
                totalCount: totalCount,
                showCount: showCount,
                limit: limit,
                callback: function (curr, limit, totalCount) {
                    createTable(curr, limit, totalCount);
                }
            });
        }

    });

}
function createTable(currPage, limit, total) {
    var html = [];
    var showNum = limit;
    if((total - (currPage * limit)) < 0){
        showNum = total - ((currPage - 1) * limit);
    }
    html.push(' <table class="table table-striped">');
    html.push(' <thead><tr>' +
        '<th>code</th>' +
        '<th>名称</th>' +
        '<th>上线时间</th>' +
        '<th>下线时间</th>' +
        '<th>操作</th>' +
        '</tr></thead><tbody>');

    $.ajax({
        type:'GET',
        url:'/show/lists?page='+currPage+'&size='+showNum,
        success:function(data){
            $.each(data,function(i,o){
                html.push('<tr><td>'+ o.code+'</td>');
                html.push('<td>' + o.name+ '</td>');
                html.push('<td>' +  FormatDate(new Date(o.releaseDate))+ '</td>');
                html.push('<td>' + FormatDate(new Date(o.offlineDate))+ '</td>');
                html.push('<td><button class="btn btn-default" data-value="'+o.id+'"  >修 改</button></td>');
                html.push('</tr>');
            });
            html.push('</tbody></table>');
            var mainObj = $('#mainContent');
            mainObj.empty();
            mainObj.html(html.join(''));



        },
        error: function(XMLHttpRequest) {
            alert(XMLHttpRequest.status);
        }
    });

}
function FormatDate (date) {
    return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate();
}

$(function($) {
    $("table button").live("click",function () {
        show_id = $(this).attr("data-value");
        code = $(this).parent().prev().prev().prev().prev().text();
        name = $(this).parent().prev().prev().prev().text();
        start_date = $(this).parent().prev().prev().text();
        end_date = $(this).parent().prev().text();
        $("#id").val(show_id);
        $("#code").val(code);
        $("#name").val(name);
        $("#start_date").val(start_date);
        $("#end_date").val(end_date);
        $("#shade").show();


    });

    $("#cal").click(function () {
        $("#shade").hide();
    });

    // $("form").submit( function () {
    //     return false;
    // });


        $("#start_date,#end_date").datetimepicker(
            {format: 'yyyy-mm-dd',
                autoclose: true,
                todayBtn: true,
                keyboardNavigation:true,
                language:'cn',minView:2
            });

});



