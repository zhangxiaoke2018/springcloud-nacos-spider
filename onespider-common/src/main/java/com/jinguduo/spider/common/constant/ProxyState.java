package com.jinguduo.spider.common.constant;

/**
 * 代理服务器状态
 * 
 * <p>状态转换图：
 * <pre>
 *   Pending
 *    ^          |
 *    |          v
 *    |   +--> Standby
 *    |   |      |
 *    |   |      v
 *    |   |  Checkout
 *    |   |   |   |
 *    |   |   |   |
 *    |   |   |   |
 *    |   |   |   |
 *    |   |   |   v
 *    |   |   |  Avaliabled
 *    |   |   |   |
 *    v   |   v   v
 *   Broken
 * </pre>
 * <p><b>注：</b>Availabled状态的出口只有Broken（Using只存在Worker不存入db）
 */
public enum ProxyState {
    Pending,    // 待处理（用Nmap扫描和curl验证）
    Standby,    // 待用（Worker验证）
    Checkout,   // 正在检查是否可用
    Holder0,
    Availabled, // 可以使用
    Broken,     // 已不能使用
    Holder1,
    Holder2,
    Vps,        // 来自于 PPPoE VPS 不验证可用性
    Kuaidaili,  // 快代理
    ;

}
