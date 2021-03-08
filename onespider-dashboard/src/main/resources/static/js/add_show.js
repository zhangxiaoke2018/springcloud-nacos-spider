/**
 * Created by yangtao on 16/8/1.
 */

function init(){
    $("input[name=url]").attr("readonly","readonly");
    $("#add_btn").attr("disabled","disabled");
    $("#add_url").hide();
}
function display(){
    $("input[name=url]").removeAttr("readonly");
    $("#add_btn").removeAttr("disabled");
    $("#add_url").show();
}
init();

$("#add_url").click(function () {

    var html = '<div class="form-group">'+
        '<label>网址:<a id="cal" href="javascript:void(0);">取消</a></label>'+
        '<input type="hidden" value="" name="code"/>'+
        '<input type="text" class="form-control" name="url" placeholder="http://www.xxx.com/xxx/xxx.html"/>'+
        '<p class="text-danger" ></p>'+
        '</div>';

    $("#add_url_div").append(html);
});

/**
 * url失去焦点时进行url校验
 */
$("input[name='url']").live("blur",function () {
    //var isverify = $('.active').find('input').val();
    //var name = $("#name").val();
    var url = $(this).val();
    var category = $("#category").val();
    var $th = $(this);
    $.ajax({
        type: "GET",
        url:"/show/url",
        data:{url:url,category:category},
        dataType: "json",
        success:function (data) {

            if(typeof(data.msg) == "undefined"){
                $th.next().html("");
                $th.parent().removeClass("has-error");
                $th.parent().addClass("has-success");
                $th.prev().val(data.code);
                $th.next().removeClass("text-danger");
                $th.next().addClass("text-success");
            }else {
                $th.next().html("");
                $th.next().removeClass("text-success");
                $th.next().addClass("text-danger");
                $th.parent().removeClass("has-success");
                $th.parent().addClass("has-error");
                $th.next().html(data.msg);
            }

        }
    });
});
$("#name").blur(function () {
    var name = $("#name").val().trim();
    if(name == ""){
        init();
        return false;
    }
    $("input[name=name]").attr("readonly","readonly");
    $.ajax({
        type: "GET",
        url:"/show/"+name,
        dataType: "json",
        success:function (data) {
            var show_name = '';
            var show_id = 0;
            $("#name").next().html("");
            var had = '该剧已在以下平台存在:';
            var ca = "";
            var rs = "";
            var linked_id = 0;
            $.each(data,function (i, o) {
                had += o.platform_name;
                show_name = o.name;
                show_id = o.id;
                linked_id = o.linkedId;
                if(i != data.length-1){
                    had += ",";
                }else {
                    ca = o.category;
                    rs = o.releaseDate;
                    had += "。是否继续添加为同一剧  <a id='same_show' href='javascript:void(0);' >是</a>";
                }
            });
            if(data != ""){
                $("#show_id").val(linked_id);
                $("#ready_show_id").val(show_id);
                $("#name").next().html(had);
                $("#same_show_desc").html("以下操作将会添加到 《"+show_name+"》 中");
                display();
                $("#category").val(ca);
                $("#release_date").val(format(new Date(rs),"yyyy-MM-dd"));
            }else {//为空则是新剧,检查bi中是否存在
                $.ajax({
                    type: "GET",
                    url:"/show/check/"+name,
                    dataType: "json",
                    success:function (data) {

                        if(data.msg != 'success'){
                            BootstrapDialog.alert(data.msg);
                            $("form").hide();
                        }else {
                            $("#show_id").val(data.show_id);
                            display();
                            $("#category").val(data.category);
                            $("#release_date").val(data.release_date);
                        }
                    },
                    error: function(XMLHttpRequest) {
                        console.log(XMLHttpRequest.status);
                        console.log("/show/check/");
                    }
                });
            }
        },
        error: function(XMLHttpRequest) {
            console.log(XMLHttpRequest.status);
            console.log("/show/name");
        }
    });
});

$("#same_show").live("click",function () {
    $("#same_show_desc").show();
    $(this).remove();
    $("#name").next().remove();
    //$("#release_div").hide();
    //$("#category_div").hide();
});

$("#cal").live("click",function () {
    $(this).parent().parent().remove();
});

$("form").submit(function () {

    var arr = [];
    for( var i =0;i< $("input[name='url']").size();i++){
        var value = $("input[name='url']").eq(i).val().trim();
        if(value == ""){
            alert("网址不许为空");
            return false;
        }
        arr.push(value);
    }
    if(isRepeat(arr)){
        alert("有重复网址,请检查!");
        return false;
    }
    return true;
});
// 验证重复元素，有重复返回true；否则返回false
function isRepeat(arr) {
    var hash = {};
    for(var i in arr) {
        if(hash[arr[i]])
        {
            return true;
        }
        // 不存在该元素，则赋值为true，可以赋任意值，相应的修改if判断条件即可
        hash[arr[i]] = true;
    }
    return false;
}

$("#releaseDate").datetimepicker({
    format: 'yyyy-mm-dd',
    autoclose: true,
    todayBtn: true,
    keyboardNavigation:true,
    language:'cn',minView:2
});


/**
 * 日期格式化
 */
function format(date, fmt) {
    var o = {
        "M+": date.getMonth() + 1, //月份
        "d+": date.getDate(), //日
        "h+": date.getHours() % 12 == 0 ? 12 : date.getHours() % 12, //小时
        "H+": date.getHours(), //小时
        "m+": date.getMinutes(), //分
        "s+": date.getSeconds(), //秒
        "q+": Math.floor((date.getMonth() + 3) / 3), //季度
        "S": date.getMilliseconds() //毫秒
    };
    var week = {
        "0": "/u65e5",
        "1": "/u4e00",
        "2": "/u4e8c",
        "3": "/u4e09",
        "4": "/u56db",
        "5": "/u4e94",
        "6": "/u516d"
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    if (/(E+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, ((RegExp.$1.length > 1) ? (RegExp.$1.length > 2 ? "/u661f/u671f" : "/u5468") : "") + week[date.getDay() + ""]);
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
}