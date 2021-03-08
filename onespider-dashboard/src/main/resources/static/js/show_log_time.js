/**
 * Created by yangtao on 16/7/20.
 */

$("#search").click(function () {

    var name = $("#name").val();
    var startDate = $("#startDate").val();
    var endDate = $("#endDate").val();

    $.ajax({
        type:'GET',
        url:'/show_log/times?name='+name+'&startDate='+startDate+'&endDate='+endDate,
        success:function(data){
            var html = ""

            $.each(JSON.parse(data),function(i,o){

                $.each(o,function (j, n) {
                    if(j == 0){
                        html +='<div class="col-md-3"><table class="table table-striped" style="text-align:center"><thead><tr ><td colspan="2">'+ n.platform_name+'</td></tr></thead><tbody><tr><td>播放量</td><td>抓取时间</td></tr>';
                    }
                    html+='<td>'+ n.play_count+'</td>';
                    html+='<td>'+ n.crawled_at+'</td>';
                    html+='</tr>';
                })
                html+='</tbody></table></div>';
            });

            $("#data").html(html);
        },
        error: function(XMLHttpRequest) {
            alert(XMLHttpRequest.status);
        }
    });
});

function FormatDate (date) {
    return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate()+" "+date.getHours()+":"+date.getMinutes();
}
