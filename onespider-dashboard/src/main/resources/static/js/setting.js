/**
 * Created by yangtao on 16/7/6.
 */

$(function($) {
    $("table button").live("click",function () {

        charset = $(this).parent().prev().text();
        jmxMonitorEnabled = $(this).parent().prev().prev().text();
        spawnUrl = $(this).parent().prev().prev().prev().text();
        exitWhenComplete = $(this).parent().prev().prev().prev().prev().text();
        emptySleepTime = $(this).parent().prev().prev().prev().prev().prev().text();
        timeOut = $(this).parent().prev().prev().prev().prev().prev().prev().text();
        retryTimes = $(this).parent().prev().prev().prev().prev().prev().prev().prev().text();
        frequency = $(this).parent().prev().prev().prev().prev().prev().prev().prev().prev().text();
        sleepTime = $(this).parent().prev().prev().prev().prev().prev().prev().prev().prev().prev().text();
        threadNum = $(this).parent().prev().prev().prev().prev().prev().prev().prev().prev().prev().prev().text();
        platformId = $(this).parent().prev().prev().prev().prev().prev().prev().prev().prev().prev().prev().prev().text();
        domain = $(this).parent().prev().prev().prev().prev().prev().prev().prev().prev().prev().prev().prev().prev().text();
        id = $(this).parent().prev().prev().prev().prev().prev().prev().prev().prev().prev().prev().prev().prev().prev().text();

        $("#id").val(id);
        $("#domain").val(domain);
        $("#platformId").val(platformId);
        $("#threadNum").val(threadNum);
        $("#sleepTime").val(sleepTime);
        $("#frequency").val(frequency);
        $("#retryTimes").val(retryTimes);
        $("#timeOut").val(timeOut);
        $("#emptySleepTime").val(emptySleepTime);
        $("#exitWhenComplete").val(exitWhenComplete);
        $("#spawnUrl").val(spawnUrl);
        $("#jmxMonitorEnabled").val(jmxMonitorEnabled);
        $("#charset").val(charset);

        $("#shade").show();
    });

    $("#cal").click(function () {
        $("#shade").hide();
    });

    $("#add_setting").click(function () {
        $("#id").val("");
        $("#domain").val("");
        $("#platformId").val("");
        $("#threadNum").val("");
        $("#sleepTime").val("");
        $("#frequency").val("");
        $("#retryTimes").val("");
        $("#timeOut").val("");
        $("#emptySleepTime").val("");
        $("#exitWhenComplete").val("");
        $("#spawnUrl").val("");
        $("#jmxMonitorEnabled").val("");
        $("#charset").val("");
        $("#shade").show();
    });


});