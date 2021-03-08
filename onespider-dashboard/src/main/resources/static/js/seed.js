$('.edit_td').click(function() {
	var id = $(this).attr("value");
	var forCtr = $(this).attr("for");
	var preValue = $("#" + forCtr).attr("value");
	var paraName = $(this).attr("name"); 			
	var input = $('<input id="attribute" type="text" class="span2" pre_value="' + preValue + '" value="' + preValue + '" />')
	$("#" + forCtr).text('').append(input);
	input.select();
	input.blur(function() {
		var preValue = $(this).parent().attr("value");				
		var curValue = $('#attribute').val();
		if(curValue == preValue){
			$('#attribute').parent().text(preValue);
			$('#attribute').remove();
			return;
		}
		if(isNaN(curValue)){
			BootstrapDialog.alert("修改的参数必须是数字!");
			$('#attribute').parent().text(preValue);
			$('#attribute').remove();
			return;
		}
		BootstrapDialog.confirm("是否将频率由【" + preValue + "】改为【" + curValue + "】?", function(result) {
			if(result){
				var data = "?id=" + id + "&" + paraName + "=" + encodeURIComponent(curValue);
				$.ajax({
	                type : 'GET',
	                url : "/seed/modify/" + data,
	                dateType : 'json',
	                success: function(res){
	                	if(res=='SUCCESS'){
							$('#attribute').parent().text(curValue);
							$('#p_' + id).attr("pre_value", curValue);
							$('#attribute').remove();
						}else{
							//show msg	 					
							BootstrapDialog.alert("修改失败.errMsg:"+res);			 					
							$('#attribute').parent().text(preValue);
							$('#attribute').remove();
						}
	                }
	            })
			}else{
				$('#attribute').parent().text(preValue);
				$('#attribute').remove();
			}
		});
	});
});

$(".status_btn").click(function(){
	var id = $(this).attr("id").substring(7);
	var status = $(this).attr("value");
	var that = $(this);
	var handle_info = status == 0 ? '删除' : '恢复';
	var new_status = status== 0 ? -1 : 0;
	BootstrapDialog.confirm("是否将ID:"+id+"的  Seed "+handle_info+"?", function(result) {
		if(result){
			var data = "?id=" + id + "&status="+new_status;
			$.ajax({
                type : 'GET',
                url : "/seed/modify/" + data,
                dateType : 'json',
                success: function(res){
                	if(res=='SUCCESS'){
                		if(status==0){
                			//原状态为 0  删除成功以后 样式与文本改变
                			that.attr("class",'btn btn-success status_btn');
                			that.text("恢复");
                			that.val(new_status);
                			//that.attr("value",new_status);
                		}else{
                			that.attr("class",'btn btn-danger status_btn');
                			that.text("删除");
                			that.val(new_status);
                		}
					}else{
						//show msg	 					
						BootstrapDialog.alert("修改失败.errMsg:"+res);
					}
                }
            })
		}
	});
})