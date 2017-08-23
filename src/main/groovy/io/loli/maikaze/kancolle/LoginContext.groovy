package io.loli.maikaze.kancolle

import com.alibaba.fastjson.JSON
import io.loli.maikaze.utils.HttpClientUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

/**
 * Created by uzuma on 2017/8/20.
 */
public class LoginContext {
    static final Logger logger = LoggerFactory.getLogger(LoginContext)

    String $1_dmm_token, $1_token, $1_html

    String $2_token, $2_login_id, $2_password, $2_html

    String $3_html

    String $4_url_from_html, $4_html, $4_owner, $4_st

    String $5_html, $5_world, $5_world_ip

    String $6_api_token, $6_api_starttime, $6_flash_url

    String user_agent
    String username;
    String password;
    public KancolleProperties properties;
    public HttpClientUtil httpClientUtil;

    def reset(username, password) {
        $1_dmm_token = null;
        $1_token = null;
        $1_html = null
        $2_token = null
        $2_login_id = null
        $2_password = null
        $2_html = null
        $3_html = null
        $4_url_from_html = null
        $4_html = null
        $4_owner = null
        $4_st = null
        $5_html = null
        $5_world = null
        $5_world_ip = null
        $6_api_token = null
        $6_api_starttime = null

        $6_flash_url = null
        this.username = username
        this.password = password
    }


    def startLogin() {
        loginToken()
        ajaxGetToken()
        login()
        getUserIdFromIframeUrl()
        getServerIpWithUserId()
        getGameTokenWithServerIp()
    }


    def loginToken() {
        def start = System.currentTimeMillis();
        def loginPageResult = httpClientUtil.get(properties.loginUrl, null, properties.loginTokenHeaders);
        $1_html = loginPageResult
        def dmmToken = (loginPageResult =~ /"DMM_TOKEN", "(.+)(?=")/)[0][1]
        def token = (loginPageResult =~ /"token": "(.+)(?=")/)[0][1]
        $1_dmm_token = dmmToken
        $1_token = token
        logger.info("GET_TOKEN:DMM_TOKEN=$dmmToken,TOKEN=$token,COST={}", (System.currentTimeMillis() - start))
    }


    def ajaxGetToken() {
        def start = System.currentTimeMillis();
        def dmmToken = $1_dmm_token
        def token = $1_token
        logger.info("AJAX_GET_TOKEN:DMM_TOKEN=$dmmToken,TOKEN=$token")
        properties.idKeysHeaders['DMM_TOKEN'] = dmmToken
        def idKeysResult = httpClientUtil.post(properties.ajaxGetTokenUrl, ['token': token], properties.idKeysHeaders);
        logger.info("AJAX_GET_TOKEN:RES=$idKeysResult,COST={}", System.currentTimeMillis() - start)
        $2_html = idKeysResult
        def resMap = JSON.parseObject(idKeysResult, Map)
        $2_login_id = resMap['login_id']
        $2_token = resMap['token']
        $2_password = resMap['password']
    }

    def login() {
        def start = System.currentTimeMillis();
        def token = $2_token
        def loginIdToken = $2_login_id
        def passwordToken = $2_password
        def params = [
                'login_id'     : username,
                'password'     : password,
                'token'        : token,
                (loginIdToken) : username,
                (passwordToken): password
        ]
        logger.info("PREPARE_LOGIN:PARAMS={$params}")
        def res = httpClientUtil.post(properties.getAuthUrl(), params, properties.loginAuthHeaders)
        if (res.contains("認証エラー")) {
            throw new DmmException("你需要在dmm网页上修改密码")
        }
        //  如果标题中包含了login，那么则需要登录
        if (res.contains("パスワードを保存する")) {
            throw new DmmException("用户名密码错误")
        }
        logger.info("LOGIN_SUCCESS:PARAMS={$params},COST={}", System.currentTimeMillis() - start)
        $3_html = res
    }

    def getUserIdFromIframeUrl() {
        def start = System.currentTimeMillis();
        def gameResult = httpClientUtil.get(properties.getGameUrl(), null, properties.simpleHeaders)
        $4_html = gameResult

        String frameUrl = (gameResult =~ /(?<=")(.*osapi.*owner=.*)(?=")/)[0][0]
        frameUrl = frameUrl.substring(0, frameUrl.lastIndexOf("#"));
        logger.info("GET_USER_ID:params=$frameUrl,COST={}", System.currentTimeMillis() - start)
        $4_url_from_html = frameUrl
        def params = UriComponentsBuilder.fromHttpUrl(frameUrl).build().getQueryParams();
        $4_owner = params['owner'][0]
        $4_st = new URLDecoder().decode(params['st'][0], 'UTF-8')
    }

    def getServerIpWithUserId() {
        def start = System.currentTimeMillis();
        def headers = properties.simpleHeaders.clone();
        headers['Referer'] = $4_url_from_html
        def format = String.format(properties.getWorldUrl, $4_owner, System.currentTimeMillis())
        logger.info("GET_SERVER_IP:$format")
        def serverIpResult = httpClientUtil.get(format, null, headers)
        logger.info("GET_SERVER_IP:$serverIpResult,COST={}", System.currentTimeMillis() - start)
        $5_html = serverIpResult
        def object = JSON.parseObject(serverIpResult.substring(7), Map)
        $5_world = object['api_data']['api_world_id'];
        $5_world_ip = properties.serverList[Integer.parseInt($5_world) - 1]
    }


    def getGameTokenWithServerIp() {
        def start = System.currentTimeMillis();
        String url = String.format(properties.getFlashUrl, $5_world_ip, $4_owner, System.currentTimeMillis())
        def params = [
                'url'         : url,
                'httpMethod'  : 'GET',
                'authz'       : 'signed',
                'st'          : $4_st,
                'contentType' : 'JSON',
                'numEntries'  : '3',
                'getSummaries': 'false',
                'signOwner'   : 'true',
                'signViewer'  : 'true',
                'gadget'      : 'http://203.104.209.7/gadget.xml',
                'container'   : 'dmm'
        ]
        logger.info("GET_GAME_TOKEN:REQ=$params")
        def makeRequestResult = httpClientUtil.get(properties.getMakeRequestUrl(), params, properties.simpleHeaders)
        logger.info("GET_GAME_TOKEN:RES=$makeRequestResult,COST={}", System.currentTimeMillis() - start)
        String availableString = makeRequestResult.substring(27)
        def obj = JSON.parseObject(availableString, Map)
        availableString = obj[url]['body'].substring(7);
        obj = JSON.parseObject(availableString, Map)
        $6_api_token = obj['api_token'];
        $6_api_starttime = obj['api_starttime']

        $6_flash_url = String.format(properties.mainFlashUrl, $5_world_ip, $6_api_token, $6_api_starttime)
        logger.info("GET_GAME_TOKEN:FLASH={}", $6_flash_url)
        $6_flash_url
    }

}
