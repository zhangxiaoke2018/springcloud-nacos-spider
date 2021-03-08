
$("#btn").click(function(){
    var showName = $("#showName").val();
    if(showName.trim() == ""){
        alert("名称不能为空");
        return;
    }
    $.ajax({
        type: 'GET',
        url: '/show/not_on_billboard/'+showName,
        success: function (data) {
            alert(data);
            $("#showName").val("");
        }
    });

});

