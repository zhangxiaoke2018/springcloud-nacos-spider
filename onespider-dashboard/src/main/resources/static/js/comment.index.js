/**
 *  评论数查询
 */
$(function () {
    /** 初始化表格 */
    Grid();
    /** 查询 */
    Serch();
});

/***
 * 初始化表格
 */
var Grid = function () {

    $("#shows_commet_count_grid").datagrid({
        url: "/comment/commentPage.json",
        data: false,
        autoload: true,
        paramsMapping: {
            page: "page",
            paging: "rows",
        },
        parse: function (data) {
            if ($.type(data) === 'string') {
                return JSON.parse(data);
            } else {
                return data;
            }
        },
        remoteSort: true,
        idField: 'id',
        rownumbers: true,
        col: [{
            field: "id",
            title: "showId",
            attrHeader: {
                "style": "text-align: center;",
                "nowrap": "nowrap"
            }
        }, {
            field: "name",
            title: "剧名",
            attrHeader: {
                "style": "text-align: center;",
                "nowrap": "nowrap"
            }
        }, {
            field: "platform_id",
            title: "平台",
            attrHeader: {
                //"width": "20%",
                "style": "text-align: center;",
                "nowrap": "nowrap"
            },
            render: function (data) {
                var platformStr = "";
                switch (data.value) {
                    case 1:
                        platformStr = "腾讯";
                        break;
                    case 2:
                        platformStr = "爱奇艺";
                        break;
                    case 3:
                        platformStr = "优酷";
                        break;
                    case 4:
                        platformStr = "土豆";
                        break;
                    case 5:
                        platformStr = "搜狐";
                        break;
                    case 6:
                        platformStr = "乐视";
                        break;
                    case 7:
                        platformStr = "芒果";
                        break;
                    case 8:
                        platformStr = "响巢看看";
                        break;
                    case 9:
                        platformStr = "风行";
                        break;
                    case 10:
                        platformStr = "56网";
                        break;
                    case 11:
                        platformStr = "PPTV";
                        break;
                    default:
                        platformStr = "";
                        break;
                }
                return platformStr;
            }
        }, {
            field: "code",
            title: "code",
            attrHeader: {
                "style": "text-align: center;",
                "nowrap": "nowrap"
            }
        }, {
            field: "episode",
            title: "集数",
            attrHeader: {
                "width": "12%",
                "style": "text-align: center;",
                "nowrap": "nowrap"
            }
        }, {
            field: "parent_id",
            title: "父Id",
            attrHeader: {
                "width": "12%",
                "style": "text-align: center;",
                "nowrap": "nowrap"
            }
        }, {
            field: "上线时间",
            title: "offline_date",
            attrHeader: {
                "width": "12%",
                "style": "text-align: center;",
                "nowrap": "nowrap"
            },
            render: function (data) {
                if (null != data.value) {
                    return new Date(parseInt(data.value)).Format("yyyy-MM-dd hh:mm:ss");
                } else {
                    return "<span class='label label-default'>----</span>";
                }
            }
        }, {
            field: "comment_count",
            title: "评论数",
            attrHeader: {
                "style": "text-align: center;",
                "nowrap": "nowrap"
            },
            attr: {"style": "width:100px"}
        }],
        onRowData: function (data, num, $tr) {
            /*if ( data.csEmail === user_name ) {
             $tr.addClass( "success" );
             } */
        },
        attr: {"class": "table table-bordered table-condensed"},
        sorter: "bootstrap",
        pager: "bootstrap",
        paramsDefault: {
            showName: "",
            platformId: "",
            catgroy:""
        },
    });

}
/***
 * 初始化查询
 */
var Serch = function () {
    /** 查询按钮 */
    var $eventsSerch = $("#btn_query");
    var $shows_commet_count_grid, fetch;
    $eventsSerch.click(function () {
        try {
            $shows_commet_count_grid = $("#shows_commet_count_grid").datagrid("datagrid");
            $shows_commet_count_grid._params.showName = $("#searchName").val().trim();
            $shows_commet_count_grid._params.platformId = "";
        } catch (e) {
            alert("查询数据失败!" + e);
            return;
        }
        fetch = $("#shows_commet_count_grid").datagrid("fetch");
    });

    /** enter查询表格事件 */
    $(".enter-key-search").keyup(function(){
        if(event.keyCode == 13){
            try {
                $shows_commet_count_grid = $("#shows_commet_count_grid").datagrid("datagrid");
                $shows_commet_count_grid._params.showName = $("#searchName").val().trim();
                $shows_commet_count_grid._params.platformId = "";
            } catch (e) {
                alert("查询数据失败!"+e);
                return;
            }
            fetch = $("#shows_commet_count_grid").datagrid("fetch");
        }
    });
}
