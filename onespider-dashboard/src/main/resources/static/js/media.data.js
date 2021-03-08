/**
 * Created by phil on 2016/8/12.
 */
var url_arry = [];
var param_url_array = [];
var param_keyword;
// bi result
var biparam = {};

$(function() {

    /** Init Page Element */
    var page = new InitMPage();
        page.initPage();

});

/**
 * 初始化媒体指数页面
 * @constructor
 */
var InitMPage = function(){};
InitMPage.prototype = {
    initPage:function () {
        $('body').find('input:first').focus();
        this.inputUrl();
        this.inputKeywords();
        this.addTask();
        this.inputEvent();
        return this;
    },
    inputUrl:function () {
        var $input = $('#officialUrl');
        var message = "可以从这里可以添加微博指数任务了！";
        $input.attr("data-content",message);
        $input.popover('show');
        setTimeout(function(){
            $input.popover('hide');
        },3500)
        return this;
    },
    inputKeywords:function () {
        /** keyworks enter event */
        var $keywords = $('#keyWords');
        var $keyWordsStor = $("#key-words-stor");
        $keywords.on({
            keypress: function(){
                if(event.keyCode == "13") {
                    keyWordsCheck($keyWordsStor,$('#keyWords'));
                }
            },
            blur: function(){
                keyWordsCheck($keyWordsStor,$('#keyWords'));
            }
        });
    },
    inputEvent: function () {
        /** keyworks enter event */
        var $officialUrl = $('#officialUrl');
        var $urlsStor = $("#urls-stor");
        $officialUrl.on({
            keypress: function(){
                if(event.keyCode == "13") {
                    urlInputCheck($urlsStor,$('#officialUrl'));
                }
            },
            blur: function(){
                urlInputCheck($urlsStor,$('#officialUrl'));
            }
        });
    },
    addTask: function () {
        /** add task process */
        $('#add-task-btn').on('click', function () {
            var $btn = $(this).button('loading')
            var param = {};
            var $urlsStor = $("#urls-stor");
            var $officialUrl = $('#officialUrl');
            var $key_word = $('#keyWords');
            var $keyWordsStor = $("#key-words-stor");

            if(null != $key_word.val().trim() && "" != $key_word.val().trim()){
                keyWordsCheck($keyWordsStor,$key_word);
            }
            if(null != $officialUrl.val().trim() && "" != $officialUrl.val().trim()){
                urlInputCheck($urlsStor,$officialUrl);
            }

            // business logic...
            /*if( url_arry.length <= 0){
                alert(" Please Insert Url At Least One And Press Enter ！");
                $btn.button('reset');
                return false;
            }*/
            if( "" === param_keyword  || null == param_keyword ){
                alert(" Please Insert KeyWord ！");
                $btn.button('reset');
                return false;
            }
            param.urls = url_arry;
            param.keyWords = param_keyword;
            param.show_id = biparam.show_id;
            param.category = biparam.category;
            param.urlJsonArray = JSON.stringify(param_url_array);
            try {
                $.post("addMediaShow.do",param,function(result){
                    switch (parseInt(result.status)) {
                        case 200 :
                            alert("success!!");
                            if (null != result.failUrl && "" != result.failUrl) {
                                //添加失败的url
                                var $urls_fail = $("#urls-fail");
                                $.each(JSON.parse(result.failUrl),function(key,value){
                                    $urls_fail.append(UrlHtml(value,key,"alert-danger"));
                                });
                            }
                            location.reload();
                            break;
                        case 500 :
                            alert("请至少填入一个url或keyword");
                            break;
                        case 404 :
                            alert(result.message);
                            console.error(result.stackTrace);
                            break;
                        case 400 :
                            alert(result.message);
                            console.error(result.stackTrace);
                            break;
                        default :
                            alert(" error : "+ JSON.stringify(result.failUrl));
                            break;
                    }
                    $btn.button('reset');
                });
            }catch ( e ) {
                console.error(e.message)
                $btn.button('reset');
            }
        })
        return this;
    }
}


/** verify  */
var Check = function (type,value) {
    var flag = false;

    switch (type){
        case 'url':
            /*var strRegex = "^((https|http|ftp|rtsp|mms)?://)"
                + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // ftp的user@
                + "(([0-9]{1,3}\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
                + "|" // 允许IP和DOMAIN（域名）
                + "([0-9a-z_!~*'()-]+\.)*" // 域名- www.
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\." // 二级域名
                + "[a-z]{2,6})" // first level domain- .com or .museum
                + "(:[0-9]{1,4})?" // 端口- :80
                + "((/?)|" // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
            var RegExp = new RegExp(strRegex);
            if (RegExp.test(value)) {
                flag = true;
            } else {
                flag = false;
            }*/
            return true;
            break;
        default:
            flag = false;
            break;
    }
    return flag;
}

/***
 * url检查公共方法
 * @param $urlsStor
 * @param $officialUrl
 * @returns {boolean}
 */
var urlInputCheck = function($urlsStor,$officialUrl){
    var $key_word = $('#keyWords');
    if( ""===param_keyword || null == param_keyword ){
        alert("请先输入剧名!");
        $key_word.focus();
        return false;
    }
    var officialUrl = $officialUrl.val().trim();
    if(null == officialUrl || "" == officialUrl){
        return false;
    }
    if( Check('url',officialUrl) ){
        $.get('checkUrl.do', {url:officialUrl,keywords:param_keyword}, function(data) {
            switch (parseInt(data.status)) {
                case 200 :
                    if( -1 === $.inArray(officialUrl, url_arry) ){
                        url_arry.push(officialUrl);

                        //装载url参数
                        var param_url = {};
                        param_url.officialUrl = officialUrl;
                        param_url.title = data.title;
                        param_url.code = data.code;

                        param_url_array.push(param_url);
                        $urlsStor.append(UrlHtml(data.title,officialUrl));
                        $officialUrl.val("");
                    }else{
                        console.log( officialUrl + "is exist!")
                        return false;
                    }
                    break;
                case 501 :
                    alert(" Please Check Url Availability! "+officialUrl);
                    $officialUrl.val("");
                    return false;
                default :
                    $officialUrl.val("");
                    return false;
            }
        });
    }
}
/**
 * 关键字生成
 * @param $keyWordsStor
 * @param $key_word
 * @returns {boolean}
 */
var keyWordsCheck = function($keyWordsStor,$key_word){
    var key_word = $key_word.val().trim();

    if( "" === key_word || null == key_word ){
        return false;
    }
    $.get('checkKeyword.do', {keyword:key_word}, function(data) {
        switch (parseInt(data.status)) {
            case 200 :
                if( -1 === $.inArray(officialUrl, url_arry) ){
                    biparam['show_id'] = data.show_id;
                    biparam['category'] = data.category;
                    biparam['release_date'] = data.release_date;
                    biparam['show_name'] = key_word;
                    param_keyword = key_word;
                    //key_words_arry.push(key_word);
                    //装载关键字参数
                    $keyWordsStor.append(KeyWordHtml(key_word,data.show_id));
                    $key_word.val("");
                    $key_word.attr("disabled","disabled");
                    //装载Bi返回结果
                    $(".bs-docs-sidenav").append(BiResultDraw(biparam));
                }else{
                    console.log( keywords + "is exist!")
                    return false;
                }
                break;
            case 400 :
                alert(data.message);
                $key_word.val("");
                return false;
            case 401 :
                alert(" Please Call System Admin! ");
                return false;
            case 500 ://Bi中不存在该剧
                alert(data.message);
                $key_word.val("");
                return false;
            case 501 :
                alert(data.message);
                $key_word.val("");
                return false;
            default :
                return false;
        }
    });
}
/***
 * bi html
 * @param bi
 * @constructor
 */
var BiResultDraw = function(bi){
    var biArray = [];
    biArray.push('<li id="'+bi.show_id+'" class="active">');
    biArray.push('    <a href="javascript:;">'+bi.show_name+'</a>');
    biArray.push('    <ul class="nav bi-result">');
    biArray.push('        <li>');
    biArray.push('            <div class="alert alert-info" role="alert">');
    biArray.push('                <strong>SowId:</strong>'+bi.show_id);
    biArray.push('            </div>');
    biArray.push('        </li>');
    biArray.push('        <li>');
    biArray.push('            <div class="alert alert-info" role="alert">');
    biArray.push('                <strong>分类:</strong>'+bi.category);
    biArray.push('            </div>');
    biArray.push('        </li>');
    biArray.push('        <li>');
    biArray.push('            <div class="alert alert-info" role="alert">');
    biArray.push('                <strong>上线时间:</strong>'+bi.release_date);
    biArray.push('            </div>');
    biArray.push('        </li>');
    biArray.push('    </ul>');
    biArray.push('</li>');
    return biArray.join("");
}
/***
 * 生成url标签（可扩展）
 * @param title
 * @param url
 * @param css
 * @returns {string}
 * @constructor
 */
var UrlHtml = function(title,url,css){
    var default_css = "alert-success";
    if(null != css && undefined != css && "" != css){
        default_css = css;
    }
    var hArray = [];
    hArray.push('');
    hArray.push('<div class="url-alert alert '+default_css+' alert-dismissible fade in"  style="padding: 3px;margin-bottom: 20px;border: 1px solid transparent;border-radius: 4px;">');
    hArray.push('    <button type="button" class="close" data-dismiss="alert" aria-label="Close" onclick="closeTag(this)" value="'+url+'" tage-type="url"><span aria-hidden="true">×</span></button>');
    hArray.push('<strong>'+title+":"+'</strong><a href="'+url+'" target="_blank">'+url+'</a>');
    hArray.push('</div>');
    return hArray.join("");
}
/**
 * 生成关键字标签
 * @param keyword
 * @returns {string}
 * @constructor
 */
var KeyWordHtml = function(keyword,bi_show_id){
    var hArray = [];
    hArray.push('');
    hArray.push('<div class="key-alert alert alert-info alert-dismissible fade in"  style="padding: 3px;margin-bottom: 20px;border: 1px solid transparent;border-radius: 4px;">');
    hArray.push('    <button type="button" class="close" data-dismiss="alert" aria-label="Close" onclick="closeTag(this)" value="'+keyword+'" tage-type="keyword" bi-show-id="'+bi_show_id+'"><span aria-hidden="true">×</span></button>');
    hArray.push('<strong>'+keyword+'</strong>');
    hArray.push('</div>');
    return hArray.join("");
}

/***
 * 关闭标签
 * @param e
 */
function closeTag(e){
    var $btn = $(e);
    var value = $btn.attr("value");
    var tag_type = $btn.attr("tage-type");
    var bi_show_id = $btn.attr("bi-show-id");
    if("url" === tag_type){
        var index = $.inArray(value, url_arry)
        url_arry.splice(index, 1);
    } else if("keyword" === tag_type){
        param_keyword = "";
        $('#keyWords').removeAttrs("disabled");
        $("#"+bi_show_id).remove();
    }
}
